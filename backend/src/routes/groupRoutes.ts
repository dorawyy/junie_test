import express from 'express';
import jwt from 'jsonwebtoken';
import Group, { IGroup } from '../models/Group';
import User, { IUser } from '../models/User';
import Restaurant from '../models/Restaurant';
import mongoose from 'mongoose';

const router = express.Router();

// Middleware to authenticate JWT token
const authenticateToken = async (req: express.Request, res: express.Response, next: express.NextFunction) => {
  try {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({ message: 'Authentication token is required' });
    }
    
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'default_secret') as { id: string };
    const user = await User.findById(decoded.id);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    // Add user to request object
    (req as any).user = user;
    next();
  } catch (error) {
    return res.status(403).json({ message: 'Invalid or expired token' });
  }
};

// Create a new group
router.post('/', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { name } = req.body;
    const user = (req as any).user as IUser;
    
    if (!name) {
      return res.status(400).json({ message: 'Group name is required' });
    }
    
    // Create new group
    const group = new Group({
      name,
      creator: user._id,
      members: [user._id],
      restaurantPreferences: new Map()
    });
    
    await group.save();
    
    // Add group to user's groups
    user.groups.push(group._id);
    await user.save();
    
    res.status(201).json(group);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Get all groups for the current user
router.get('/', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const user = (req as any).user as IUser;
    
    // Find all groups where the user is a member
    const groups = await Group.find({ members: user._id });
    
    res.status(200).json(groups);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Get a group by ID
router.get('/:id', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;
    const user = (req as any).user as IUser;
    
    const group = await Group.findById(id);
    
    if (!group) {
      return res.status(404).json({ message: 'Group not found' });
    }
    
    // Check if user is a member of the group
    if (!group.members.some(memberId => memberId.toString() === user._id.toString())) {
      return res.status(403).json({ message: 'You are not a member of this group' });
    }
    
    res.status(200).json(group);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Join a group by code
router.post('/join', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { code } = req.body;
    const user = (req as any).user as IUser;
    
    if (!code) {
      return res.status(400).json({ message: 'Group code is required' });
    }
    
    // Find group by code
    const group = await Group.findOne({ code });
    
    if (!group) {
      return res.status(404).json({ message: 'Group not found' });
    }
    
    // Check if user is already a member
    if (group.members.some(memberId => memberId.toString() === user._id.toString())) {
      return res.status(400).json({ message: 'You are already a member of this group' });
    }
    
    // Add user to group
    group.members.push(user._id);
    await group.save();
    
    // Add group to user's groups
    user.groups.push(group._id);
    await user.save();
    
    res.status(200).json(group);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Submit restaurant preferences for a group
router.post('/:id/preferences', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;
    const { likedRestaurantIds } = req.body;
    const user = (req as any).user as IUser;
    
    if (!Array.isArray(likedRestaurantIds)) {
      return res.status(400).json({ message: 'likedRestaurantIds must be an array' });
    }
    
    const group = await Group.findById(id);
    
    if (!group) {
      return res.status(404).json({ message: 'Group not found' });
    }
    
    // Check if user is a member of the group
    if (!group.members.some(memberId => memberId.toString() === user._id.toString())) {
      return res.status(403).json({ message: 'You are not a member of this group' });
    }
    
    // Set user's preferences
    group.restaurantPreferences.set(user._id.toString(), likedRestaurantIds);
    
    // Check if all members have voted and find a match if they have
    if (group.allMembersVoted()) {
      const matchedRestaurantId = group.findBestMatch();
      if (matchedRestaurantId) {
        group.matchedRestaurantId = matchedRestaurantId;
      }
    }
    
    await group.save();
    
    res.status(200).json(group);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Get the matched restaurant for a group
router.get('/:id/match', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;
    const user = (req as any).user as IUser;
    
    const group = await Group.findById(id);
    
    if (!group) {
      return res.status(404).json({ message: 'Group not found' });
    }
    
    // Check if user is a member of the group
    if (!group.members.some(memberId => memberId.toString() === user._id.toString())) {
      return res.status(403).json({ message: 'You are not a member of this group' });
    }
    
    // Check if a match has been found
    if (!group.matchedRestaurantId) {
      // If all members have voted but no match was found
      if (group.allMembersVoted()) {
        return res.status(404).json({
          matched: false,
          message: 'No match found. Try again with different preferences.'
        });
      } else {
        // If not all members have voted yet
        return res.status(202).json({
          matched: false,
          message: 'Waiting for all members to submit their preferences.'
        });
      }
    }
    
    // Get the matched restaurant
    const restaurant = await Restaurant.findById(group.matchedRestaurantId);
    
    if (!restaurant) {
      return res.status(404).json({
        matched: false,
        message: 'Matched restaurant not found.'
      });
    }
    
    res.status(200).json({
      matched: true,
      restaurant
    });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Leave a group
router.post('/:id/leave', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;
    const user = (req as any).user as IUser;
    
    const group = await Group.findById(id);
    
    if (!group) {
      return res.status(404).json({ message: 'Group not found' });
    }
    
    // Check if user is a member of the group
    if (!group.members.some(memberId => memberId.toString() === user._id.toString())) {
      return res.status(403).json({ message: 'You are not a member of this group' });
    }
    
    // Remove user from group
    group.members = group.members.filter(memberId => memberId.toString() !== user._id.toString());
    
    // If the user was the creator and there are other members, assign a new creator
    if (group.creator.toString() === user._id.toString() && group.members.length > 0) {
      group.creator = group.members[0];
    }
    
    // If there are no more members, delete the group
    if (group.members.length === 0) {
      await Group.findByIdAndDelete(id);
      
      // Remove group from user's groups
      user.groups = user.groups.filter(groupId => groupId.toString() !== id);
      await user.save();
      
      return res.status(200).json({ message: 'Group deleted successfully' });
    }
    
    // Remove user's preferences
    group.restaurantPreferences.delete(user._id.toString());
    
    await group.save();
    
    // Remove group from user's groups
    user.groups = user.groups.filter(groupId => groupId.toString() !== id);
    await user.save();
    
    res.status(200).json({ message: 'Left group successfully' });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

export default router;