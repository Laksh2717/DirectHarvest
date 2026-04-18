# DirectHarvest Frontend

A modern, responsive Next.js application serving as the primary interface for farmers and buyers to interact with the DirectHarvest marketplace platform.

---

## Tech Stack

| Technology | Purpose | Version |
|---|---|---|
| **Next.js** | Server-side rendering, App Router, optimized production builds | 16.2.2 |
| **React** | UI library with hooks and concurrent features | 19.2.4 |
| **TypeScript** | Type-safe development and better IDE support | Latest |
| **Tailwind CSS** | Utility-first CSS framework for rapid UI development | v4 |
| **Axios** | HTTP client for API communication with backend | 1.14.0 |
| **React Hook Form** | Efficient form state management and validation | 7.72.1 |
| **Sonner** | Toast notifications, async state management | sonner@2.0.7 |
| **Lucide Icons** | Lightweight icon library for consistent UI | 1.7.0 |
| **Google OAuth** | Social authentication for sign-in | @react-oauth/google@0.12.2 |
| **Cloudinary** | Cloud-based image upload and storage | Integration via API |
| **Recharts** | Data visualization for dashboard analytics | 3.8.1 |
| **ESLint** | Code quality and style enforcement | Latest |

---

## Project Structure

```
frontend/
├── src/
│   ├── app/                          
│   │   ├── layout.tsx                
│   │   ├── page.tsx   
│   │   ├── globals.css               
│   │   ├── browse/                   
│   │   ├── login/[role]/             
│   │   ├── register/[role]             
│   │   ├── buyer/                    
│   │   │   ├── active-orders/        
│   │   │   ├── browse-products/      
│   │   │   ├── offers/               
│   │   │   ├── completed-orders/     
│   │   │   ├── cancelled-orders/     
│   │   │   └── profile/              
│   │   └── farmer/                   
│   │       ├── active-orders/        
│   │       ├── completed-orders/     
│   │       ├── cancelled-orders/     
│   │       ├── listings/             
│   │       ├── create-listing/            
│   │       ├── ratings/              
│   │       └── profile/              
│   │
│   ├── components/                   
│   │   ├── auth/                     
│   │   ├── browse/                   
│   │   ├── buyer/                    
│   │   ├── farmer/                   
│   │   ├── dashboard/                
│   │   ├── listings/     
│   │   ├── landing/            
│   │   ├── offers/                   
│   │   ├── orders/                   
│   │   ├── modals/                   
│   │   ├── profile/                  
│   │   ├── providers/                
│   │   └── ui/                       
│   │
│   ├── hooks/                        
│   │   ├── auth/                     
│   │   ├── browse/                   
│   │   ├── dashboard/                
│   │   ├── listings/                 
│   │   ├── offers/                   
│   │   ├── orders/                   
│   │   ├── modals/                   
│   │   ├── profile/                  
│   │
│   ├── services/                     
│   │   ├── authService.ts            
│   │   ├── userService.ts            
│   │   ├── listingService.ts         
│   │   ├── negotiationService.ts     
│   │   ├── orderService.ts           
│   │   ├── dashboardService.ts       
│   │   ├── ratingService.ts          
│   │   └── sessionService.ts         
│   │
│   ├── lib/                          
│   │   ├── api.ts                    
│   │   ├── authContext.ts            
│   │   ├── utils.ts                  
│   │   ├── formatters.ts             
│   │   ├── validators.ts             
│   │   ├── cloudinary.ts             
│   │   ├── badges.ts                 
│   │   └── api/                      
│   │
│   └── types/                        
│       ├── auth.ts                   
│       ├── user.ts                   
│       ├── listing.ts                
│       ├── offer.ts                  
│       ├── order.ts                  
│       ├── browse.ts                 
│       ├── dashboard.ts              
│       ├── rating.ts                 
│       ├── modal.ts                  
│       └── common.ts                 
│   
│
├── public/                           
├── next.config.ts                    
├── tsconfig.json                     
├── tailwind.config.ts                
├── eslint.config.mjs                 
├── postcss.config.mjs                
└── package.json                      
```

---

## Key Features

### 1. **Role-Based Authentication**
- Separate login flows for farmers and buyers
- Google OAuth integration for social sign-in
- JWT token-based session management
- Protected routes with role-based access control

### 2. **Product Browsing & Search**
- Advanced filtering (price, ratings, timeline)
- Real-time search with debouncing
- Pagination support for large datasets
- Product detail view with seller ratings

### 3. **Offer & Negotiation System**
- Place offers on listings
- Counter-offer mechanism for price negotiation
- Real-time offer status tracking
- Auto-expiry (72 hours)

### 4. **Order Management**
- Active orders tracking (buyer & farmer views)
- Completed orders with delivery history
- Cancelled orders with reason tracking
- Order timeline and status updates

### 5. **Farmer Features**
- Create and edit product listings
- Upload images via Cloudinary
- View sales analytics and dashboards
- Manage inventory and pricing
- Track buyer ratings

### 6. **Buyer Features**
- Browse products with advanced filters
- Make and track offers
- View order history and status
- Rate completed purchases
- Seller profile and ratings

---

## Setup & Installation

### Prerequisites
- Node.js 18+ and npm/yarn
- Backend API running on `http://localhost:8080` (or configured URL)
- Google OAuth credentials (Client ID)
- Cloudinary account for image uploads

### Environment Setup

Create a `.env.local` file in the `frontend/` directory:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_ACCESS_TOKEN_REFRESH_BUFFER_MS=30000
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_google_client_id_here
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloud_name
```

### Installation & Running

```bash
# Install dependencies
npm install

# Development server (with hot reload)
npm run dev

# Access at http://localhost:3000

# Build for production
npm run build

# Start production server
npm start

# Lint code
npm run lint
```

---

## Authentication Flow

```
User selects role (Farmer/Buyer)
           ↓
    Login Page Rendered
           ↓
    [Google OAuth] or [Email/Password]
           ↓
    Backend validates & returns JWT token
           ↓
    Token stored in localStorage + httpOnly cookie
           ↓
    AuthContext updated with user data
           ↓
    Redirected to role-specific dashboard
           ↓
    All API requests include JWT in Authorization header
```

### Session Management
- **Token Storage**: JWT stored in localStorage for persistence
- **Automatic Refresh**: Token refresh handled via interceptors
- **Logout**: Clear tokens and reset auth context
- **Protected Routes**: useAuth hook validates user role before rendering

---

## API Integration

### Axios Configuration
- **Base URL**: Configured via environment variables
- **Timeout**: 30 seconds default
- **Interceptors**: 
  - Request: Adds JWT token to headers
  - Response: Handles 401 errors and token refresh
  - Error handling with toast notifications

### Service Layer Pattern
Each service exports functions for specific endpoints:

```typescript
// Example: listingService.ts
export const listingService = {
  getAll: (filters?: FilterParams) => api.get('/listings', { params: filters }),
  getById: (id: string) => api.get(`/listings/${id}`),
  create: (data: CreateListingDTO) => api.post('/listings', data),
  update: (id: string, data: UpdateListingDTO) => api.put(`/listings/${id}`, data),
  delete: (id: string) => api.delete(`/listings/${id}`),
};
```

---

## Form Handling

### React Hook Form Integration
- Lightweight form state management
- Real-time validation with custom rules
- Error display and field-level feedback
- Efficient re-renders (only changed fields update)

### Validation Examples
```typescript
// Registration form validation
{
  email: { required: true, pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
  password: { required: true, minLength: 8 },
  phone: { required: true, pattern: /^\d{10}$/ },
}
```

---

## Data Flow

```
Component
    ↓
useHook (fetches data, manages state)
    ↓
Service Layer (API calls)
    ↓
Axios Instance (handles auth, errors)
    ↓
Backend API
    ↓
[Response] → Hook → Component renders
```

---

## Build & Deployment

### Docker Build

The frontend uses a three-stage Dockerfile:

| Stage | Base Image | What it does |
|---|---|---|
| deps | node:20-alpine | Runs `npm ci` — node_modules cached as separate layer |
| builder | node:20-alpine | Receives `NEXT_PUBLIC_*` as build args → bakes them in → `npm run build` |
| runner | node:20-alpine | Copies only `.next/`, `public/`, `package.json`, `node_modules`, `next.config.ts` → `npm start` |

The three-stage approach avoids shipping dev dependencies and build cache into the final image, resulting in a significantly smaller production image.

In CI/CD, the image is built with:

```bash
docker build \
  --build-arg NEXT_PUBLIC_API_URL=https://<ec2_ip>.nip.io/api \
  --build-arg NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloud \
  --build-arg NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_client_id \
  -t laksh2717/frontend:ec2 ./frontend
```

All `NEXT_PUBLIC_*` variables are baked into the image at build time — not runtime. Changing them requires a rebuild and redeploy.

See the root [README](../README.md) for full deployment instructions via Docker Compose, Ansible, and GitHub Actions.

---

## Related Documentation

- Backend API Documentation: [Backend README](../backend/README.md)
- Project Overview: [Root README](../README.md)
- Deployment Guide: See `docker-compose.yml` and Ansible playbooks
