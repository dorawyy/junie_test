import mongoose, { Document, Schema } from 'mongoose';
import crypto from 'crypto';

export interface IGroup extends Document {
  name: string;
  code: string;
  creator: mongoose.Types.ObjectId;
  members: mongoose.Types.ObjectId[];
  restaurantPreferences: Map<string, string[]>; // userId -> [restaurantId, restaurantId, ...]
  matchedRestaurantId?: mongoose.Types.ObjectId;
  createdAt: Date;
  updatedAt: Date;
  expiresAt?: Date;
  allMembersVoted(): boolean;
  findBestMatch(): mongoose.Types.ObjectId | null;
}

const GroupSchema: Schema = new Schema(
  {
    name: {
      type: String,
      required: [true, 'Group name is required'],
      trim: true
    },
    code: {
      type: String,
      required: true,
      unique: true,
      default: () => crypto.randomBytes(3).toString('hex').toUpperCase()
    },
    creator: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true
    },
    members: [{
      type: Schema.Types.ObjectId,
      ref: 'User'
    }],
    restaurantPreferences: {
      type: Map,
      of: [String]
    },
    matchedRestaurantId: {
      type: Schema.Types.ObjectId,
      ref: 'Restaurant',
      default: null
    },
    expiresAt: {
      type: Date,
      default: () => new Date(Date.now() + 24 * 60 * 60 * 1000) // 24 hours from now
    }
  },
  {
    timestamps: true
  }
);

// Method to check if all members have voted
GroupSchema.methods.allMembersVoted = function(): boolean {
  const memberIds = this.members.map((id: mongoose.Types.ObjectId) => id.toString());
  const votedMemberIds = Array.from(this.restaurantPreferences.keys());
  
  return memberIds.every(memberId => votedMemberIds.includes(memberId));
};

// Method to find the best match
GroupSchema.methods.findBestMatch = function(): mongoose.Types.ObjectId | null {
  if (!this.allMembersVoted()) return null;
  
  // Count likes for each restaurant
  const restaurantLikes = new Map<string, number>();
  
  for (const [_, likedRestaurants] of this.restaurantPreferences.entries()) {
    for (const restaurantId of likedRestaurants) {
      const currentLikes = restaurantLikes.get(restaurantId) || 0;
      restaurantLikes.set(restaurantId, currentLikes + 1);
    }
  }
  
  // Find the restaurant with the most likes
  let bestMatchId: string | null = null;
  let maxLikes = 0;
  
  for (const [restaurantId, likes] of restaurantLikes.entries()) {
    if (likes > maxLikes) {
      maxLikes = likes;
      bestMatchId = restaurantId;
    }
  }
  
  return bestMatchId ? new mongoose.Types.ObjectId(bestMatchId) : null;
};

export default mongoose.model<IGroup>('Group', GroupSchema);