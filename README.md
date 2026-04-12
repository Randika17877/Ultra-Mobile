# UltraMobile - Modern E-Commerce Android App

UltraMobile is a high-performance, dark-themed e-commerce application built with Java and Firebase. It features a sleek "Neon Dark" UI and robust shopping capabilities.

## 🚀 Tech Stack

- **Language:** Java
- **UI Framework:** Material Design 3 (MD3)
- **Backend:** Firebase (Auth, Firestore, Storage)
- **Local Database:** SQLite (for Wishlist & Offline caching)
- **Image Loading:** Glide
- **Animation:** Lottie & Custom transitions

## 📊 Project Progress

### 1. Authentication & User Profile (100%)
- [x] Firebase Email/Password Sign-in & Sign-up.
- [x] Profile Management (Edit Name, Profile Picture).
- [x] Multi-source Image Upload (Camera & Gallery).

### 2. Product Discovery (90%)
- [x] Dynamic Home Screen with Categories & Featured Products.
- [x] Case-Insensitive Product Search (Local filtering logic).
- [x] Category-based filtering.

### 3. Product Details & Selection (80%)
- [x] Dynamic Product Detail Page (`ProductDetailsFragment`).
- [x] Attribute Selection (Color, Size, RAM, ROM via ChipGroups).
- [x] Real-time data loading from Firestore.

### 4. Shopping Cart & Wishlist (95%)
- [x] Local SQLite-powered Wishlist (Fast & Offline-ready).
- [x] Real-time Shopping Cart Management.
- [x] Item quantity management.

### 5. Checkout & Orders (85%)
- [x] Multi-step Checkout process.
- [x] Shipping & Billing Address Management.
- [x] **Order History:** Complete history with status tracking (PAID, PENDING, SHIPPED).
- [ ] Payment Gateway Integration (PayHere SDK - in progress).

## 🛠 Database Schema (Firestore)

- **`users`**: User profiles, roles, and basic info.
- **`products`**: Product details, pricing, and image URLs.
- **`categories`**: Category hierarchy and icons.
- **`orders`**: Transaction records, item snapshots, and delivery status.

## 📱 Sensors & Special Features
- **Shake to Close:** Detects a sharp movement to exit the app safely using the Accelerometer.
- **Biometric Ready:** Logic prepared for secure login.

---
*Developed by lk.randika*
