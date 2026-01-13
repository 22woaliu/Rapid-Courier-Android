# Courier Service App

A comprehensive Android application for an on-demand courier service. This app connects Clients, Riders, and Administrators to facilitate package delivery, real-time order tracking, and user management.

## 📱 Features

### 👤 User Roles & Authentication
- **Secure Sign Up & Login**: Email and Password authentication via Firebase.
- **Role-Based Access**: Distinct dashboards and features for Clients, Riders, and Admins.

### 📦 Client Features
- **Place Orders**: Request a courier by providing pickup/delivery details, package weight, and contact info.
- **Order Tracking**: View status of active orders (Pending, Picked, In Transit, Delivered).
- **History**: Access past order history.

### 🛵 Rider Features
- **Job Board**: View available orders in real-time.
- **Order Management**: Accept orders, mark as "Picked Up", and "Delivered".
- **Dashboard**: Track completed deliveries and earnings (visuals).

### 🛡️ Admin Dashboard
- **Overview**: Real-time counters for Total Orders and Total Users.
- **Manage Orders**:
    - View all orders with detailed information (Sender, Receiver, Rider, Status).
    - Filter orders by status (Pending, Picked, Delivered, etc.).
- **Manage Users**:
    - View list of all Clients and Riders.
    - Filter by User Type.
    - **Delete Users**: Remove users and their associated data (Cascading delete implementation: Deleting a client removes their orders; Deleting a rider preserves delivered order history).

## 🛠️ Tech Stack

- **Language**: Java
- **UI**: XML (Android Views)
- **Backend**: Firebase Realtime Database
- **Authentication**: Firebase Authentication
- **Architecture**: MVC (Model-View-Controller)

## 🚀 Setup & Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yourusername/courier-app.git
    ```
2.  **Open in Android Studio**:
    - Open Android Studio -> File -> Open -> Select the project directory.
3.  **Firebase Configuration**:
    - Create a project in the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android App with the package name `com.example.example` (or check `AndroidManifest.xml`).
    - Download `google-services.json` and place it in the `app/` directory.
    - Enable **Authentication** (Email/Password).
    - Enable **Realtime Database** and set appropriate rules.
4.  **Build & Run**:
    - Sync Gradle files.
    - Run the app on an Emulator or Physical Device.

## 📂 Project Structure

- `activities/`: Contains all Activity classes (Screens).
- `adapters/`: RecyclerView adapters for Lists (Orders, Users).
- `models/`: Data models (User, Order, Client, Rider).
- `layout/`: XML UI definitions.

## 📝 Database Structure (Firebase)

- `users/{uid}`: Base user profile (Email, Role, Phone).
- `clients/{uid}`: Client specific details.
- `riders/{uid}`: Rider specific details (Token).
- `orders/{orderId}`: Order objects containing status, addresses, and user links.

## ⚠️ Notes

- **Rider Registration**: Requires a specific "Rider Token" during sign-up to verify employment.
- **Deletion Policy**: Admin deletion of a Client is permanent and removes all their active order history.
