import mongoose, { Document, Schema } from 'mongoose';

export interface IReview extends Document {
  userId: mongoose.Types.ObjectId;
  userName: string;
  rating: number;
  comment: string;
  date: Date;
}

export interface IRestaurant extends Document {
  name: string;
  imageUrl: string;
  cuisine: string;
  priceRange: string;
  rating: number;
  address: string;
  location: {
    type: string;
    coordinates: [number, number]; // [longitude, latitude]
  };
  phone?: string;
  website?: string;
  hours?: Map<string, string>;
  reviews: IReview[];
  googlePlaceId?: string;
  createdAt: Date;
  updatedAt: Date;
}

const ReviewSchema: Schema = new Schema({
  userId: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  userName: {
    type: String,
    required: true
  },
  rating: {
    type: Number,
    required: true,
    min: 1,
    max: 5
  },
  comment: {
    type: String,
    required: true
  },
  date: {
    type: Date,
    default: Date.now
  }
});

const RestaurantSchema: Schema = new Schema(
  {
    name: {
      type: String,
      required: [true, 'Restaurant name is required'],
      trim: true
    },
    imageUrl: {
      type: String,
      required: [true, 'Restaurant image URL is required']
    },
    cuisine: {
      type: String,
      required: [true, 'Cuisine type is required'],
      trim: true
    },
    priceRange: {
      type: String,
      required: [true, 'Price range is required'],
      enum: ['$', '$$', '$$$', '$$$$']
    },
    rating: {
      type: Number,
      required: [true, 'Rating is required'],
      min: 1,
      max: 5
    },
    address: {
      type: String,
      required: [true, 'Address is required'],
      trim: true
    },
    location: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point'
      },
      coordinates: {
        type: [Number], // [longitude, latitude]
        required: [true, 'Coordinates are required']
      }
    },
    phone: {
      type: String,
      trim: true
    },
    website: {
      type: String,
      trim: true
    },
    hours: {
      type: Map,
      of: String
    },
    reviews: [ReviewSchema],
    googlePlaceId: {
      type: String,
      trim: true
    }
  },
  {
    timestamps: true
  }
);

// Create a geospatial index on the location field
RestaurantSchema.index({ location: '2dsphere' });

export default mongoose.model<IRestaurant>('Restaurant', RestaurantSchema);