import express from 'express';
import jwt from 'jsonwebtoken';
import Restaurant, { IRestaurant } from '../models/Restaurant';
import User, { IUser } from '../models/User';
import fetch from 'node-fetch';

const router = express.Router();

// Middleware to authenticate JWT token (copied from userRoutes.ts)
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

// Get restaurants near a location
router.get('/', async (req: express.Request, res: express.Response) => {
  try {
    const { latitude, longitude, radius = 5000, cuisine } = req.query;

    if (!latitude || !longitude) {
      return res.status(400).json({ message: 'Latitude and longitude are required' });
    }

    // Convert to numbers
    const lat = parseFloat(latitude as string);
    const lng = parseFloat(longitude as string);
    const rad = parseInt(radius as string);

    // Build query
    const query: any = {
      location: {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [lng, lat] // MongoDB uses [longitude, latitude]
          },
          $maxDistance: rad
        }
      }
    };

    // Add cuisine filter if provided
    if (cuisine) {
      query.cuisine = cuisine;
    }

    // Find restaurants
    const restaurants = await Restaurant.find(query);

    res.status(200).json(restaurants);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Get restaurant by ID
router.get('/:id', async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;

    const restaurant = await Restaurant.findById(id);

    if (!restaurant) {
      return res.status(404).json({ message: 'Restaurant not found' });
    }

    res.status(200).json(restaurant);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Add a review to a restaurant
router.post('/:id/reviews', authenticateToken, async (req: express.Request, res: express.Response) => {
  try {
    const { id } = req.params;
    const { rating, comment } = req.body;
    const user = (req as any).user as IUser;

    // Validate input
    if (!rating || !comment) {
      return res.status(400).json({ message: 'Rating and comment are required' });
    }

    if (rating < 1 || rating > 5) {
      return res.status(400).json({ message: 'Rating must be between 1 and 5' });
    }

    const restaurant = await Restaurant.findById(id);

    if (!restaurant) {
      return res.status(404).json({ message: 'Restaurant not found' });
    }

    // Create review
    const review = {
      userId: user._id,
      userName: user.name,
      rating,
      comment,
      date: new Date()
    };

    // Add review to restaurant
    restaurant.reviews.push(review);

    // Update restaurant rating
    const totalRatings = restaurant.reviews.reduce((sum, review) => sum + review.rating, 0);
    restaurant.rating = totalRatings / restaurant.reviews.length;

    await restaurant.save();

    res.status(201).json(restaurant);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

// Fetch restaurants from Google Places API (admin only)
router.post('/fetch-from-google', async (req: express.Request, res: express.Response) => {
  try {
    // This endpoint would be protected in a real application
    // It's for demonstration purposes only

    const { latitude, longitude, radius = 5000 } = req.body;

    if (!latitude || !longitude) {
      return res.status(400).json({ message: 'Latitude and longitude are required' });
    }

    const apiKey = process.env.GOOGLE_PLACES_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ message: 'Google Places API key is not configured' });
    }

    // Fetch restaurants from Google Places API
    const url = `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${latitude},${longitude}&radius=${radius}&type=restaurant&key=${apiKey}`;

    const response = await fetch(url);
    const data = await response.json();

    if (data.status !== 'OK') {
      return res.status(500).json({ message: 'Error fetching data from Google Places API' });
    }

    // Process and save restaurants
    const savedRestaurants = [];

    for (const place of data.results) {
      // Check if restaurant already exists
      const existingRestaurant = await Restaurant.findOne({ googlePlaceId: place.place_id });

      if (existingRestaurant) {
        savedRestaurants.push(existingRestaurant);
        continue;
      }

      // Create new restaurant
      const restaurant = new Restaurant({
        name: place.name,
        imageUrl: place.photos && place.photos.length > 0 
          ? `https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${place.photos[0].photo_reference}&key=${apiKey}`
          : 'https://via.placeholder.com/400x300?text=No+Image',
        cuisine: place.types[0].replace('_', ' '),
        priceRange: place.price_level ? '$'.repeat(place.price_level) : '$',
        rating: place.rating || 3.0,
        address: place.vicinity,
        location: {
          type: 'Point',
          coordinates: [place.geometry.location.lng, place.geometry.location.lat]
        },
        googlePlaceId: place.place_id
      });

      await restaurant.save();
      savedRestaurants.push(restaurant);
    }

    res.status(200).json(savedRestaurants);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
});

export default router;
