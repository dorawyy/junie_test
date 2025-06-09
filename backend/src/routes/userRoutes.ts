import express from 'express';
import jwt from 'jsonwebtoken';
import User, { IUser } from '../models/User';
import { authenticateToken } from '../middleware/auth';

const router = express.Router();

// Register a new user
router.post('/register', async (req: express.Request, res: express.Response) => {
  try {
    const { name, email, password } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(400).json({ message: 'User with this email already exists' });
    }

    // Create new user
    const user = new User({
      name,
      email,
      password
    });

    await user.save();

    // Generate JWT token
    const token = jwt.sign(
      { id: user._id },
      process.env.JWT_SECRET || 'default_secret',
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    // Return user data (excluding password) and token
    const userResponse = {
      id: user._id,
      name: user.name,
      email: user.email,
      profileImageUrl: user.profileImageUrl,
      groups: user.groups
    };

    res.status(201).json({
      token,
      user: userResponse
    });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Login user
router.post('/login', async (req: express.Request, res: express.Response) => {
  try {
    const { email, password } = req.body;

    // Find user by email
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password);
    if (!isPasswordValid) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    // Generate JWT token
    const token = jwt.sign(
      { id: user._id },
      process.env.JWT_SECRET || 'default_secret',
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    // Return user data (excluding password) and token
    const userResponse = {
      id: user._id,
      name: user.name,
      email: user.email,
      profileImageUrl: user.profileImageUrl,
      groups: user.groups
    };

    res.status(200).json({
      token,
      user: userResponse
    });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Get current user profile
router.get('/me', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const user = (req as any).user as IUser;

    // Return user data (excluding password)
    const userResponse = {
      id: user._id,
      name: user.name,
      email: user.email,
      profileImageUrl: user.profileImageUrl,
      groups: user.groups
    };

    res.status(200).json(userResponse);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Update user profile
router.put('/me', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const user = (req as any).user as IUser;
    const { name, profileImageUrl } = req.body;

    // Update user fields
    if (name) user.name = name;
    if (profileImageUrl) user.profileImageUrl = profileImageUrl;

    await user.save();

    // Return updated user data (excluding password)
    const userResponse = {
      id: user._id,
      name: user.name,
      email: user.email,
      profileImageUrl: user.profileImageUrl,
      groups: user.groups
    };

    res.status(200).json(userResponse);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

export default router;
