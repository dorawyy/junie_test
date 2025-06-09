import express from 'express';
import jwt from 'jsonwebtoken';
import User from '../models/User';

/**
 * Middleware to authenticate JWT token
 */
export const authenticateToken = async (req: express.Request, res: express.Response, next: express.NextFunction) => {
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