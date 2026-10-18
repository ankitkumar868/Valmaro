# 🛍️ Valmaro — AI-Based Smart E-Commerce App

**Valmaro** is an AI-powered Android e-commerce application developed using Kotlin, XML, Firebase Realtime Database, and Google Gemini API. It aims to make online shopping smarter by helping users find suitable products and understand potential future price changes.

This project is developed as a college project and portfolio showcase. Most major features are working, while some bugs and improvements are still being addressed.

## ✨ Key Features

### 🤖 AI-Powered Shopping
- Search for products using natural-language requirements.
- Get personalized product suggestions based on user needs.
- Discover a shortlist of 2–3 suitable products.
- Compare products based on price and rating.
- Search products using regular keywords.

### 📈 AI-Based Price Prediction
- View current product prices.
- Get estimated future prices using Google Gemini API.
- Explore potential price increases or decreases.
- View the estimated best time to buy a product.
- Review price-prediction information and historical price data where available.

*Note: Predictions are estimates, not guaranteed future prices.*

### 🛒 E-Commerce Features
- Home screen and product categories.
- Product details and ratings.
- Discounted prices and offers.
- Shopping cart.
- Checkout.
- Order management and order history.
- User profile.
- Firebase-backed product information.

### 🏪 Seller Panel
- Add products.
- Edit and delete products.
- Manage orders.
- Update product stock and inventory.
- View sales information.
- Manage product prices and discounts.

### 🛡️ Admin Panel
- Manage products.
- Manage orders.
- Access controlled through Firebase UID checks.

## 🧰 Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML | User interface and layouts |
| Android Studio | Development environment |
| Firebase Realtime Database | Store and retrieve application data |
| Google Gemini API | AI-powered features and price-prediction functionality |

## 🏗️ Project Architecture

Valmaro is an Android application that uses Firebase for cloud data storage and Google Gemini API for AI-related functionality.

```text
                 VALMARO
                    |
          Android Application
             Kotlin + XML
                    |
        +-----------+-----------+
        |           |           |
      Shopping     Firebase    Gemini API
      Features     Realtime    AI Features
                    Database
        |           |
     Products    Orders, Users,
     Cart        Stock, Sellers,
     Checkout    Price History
```

## ⚙️ Requirements

Before running the project, install or arrange the following:

- Latest or recent version of Android Studio.
- Android SDK and required Gradle dependencies.
- Android device or emulator running Android 7.0 (API 24) or later.
- A Firebase project with Realtime Database configured.
- A Google Gemini API key configured securely for the AI features.
- Internet connectivity for cloud-based functionality.

## 🚀 Installation and Setup

### 1. Clone the repository

Open a terminal and run:

```bash
https://github.com/ankitkumar868/Valmaro
```

Alternatively, download the repository as a ZIP file from GitHub and extract it.

### 2. Open the project

1. Launch Android Studio.
2. Select **Open**.
3. Choose the extracted or cloned Valmaro project folder.
4. Wait for Android Studio to load the project.

### 3. Sync Gradle

Allow Android Studio to download and configure the required dependencies. If prompted, select **Sync Now**.

If Gradle synchronization fails, verify your internet connection and the project's Gradle and Android Gradle Plugin compatibility.

### 4. Configure Firebase

1. Create or select a Firebase project.
2. Register the Android application using the package name `com.example.valmaro`, if that is the package configured in the project.
3. Download the corresponding `google-services.json`.
4. Place it in the Android app module directory, typically:

   `app/google-services.json`

5. Enable Firebase Realtime Database and configure its security rules.
6. Confirm that the Firebase configuration and dependencies match the project.

**Important:** Use your own Firebase project and appropriate database permissions. Never make the database publicly writable for convenience.

### 5. Configure Google Gemini API

Valmaro uses Google Gemini API for AI-related functionality.

For local development:

1. Obtain an API key through the official Google AI developer platform.
2. Move the key out of the Kotlin source code and into an appropriate secure configuration.
3. Ensure that the key is not committed to GitHub.
4. Configure usage restrictions and monitoring where supported.

For production applications, prefer calling Gemini through a secure backend rather than embedding a reusable secret in the Android app. A key placed directly in an APK can potentially be extracted.

### 6. Run the application

1. Connect an Android device or start an emulator.
2. Enable developer options and USB debugging if using a physical device.
3. Select the device in Android Studio.
4. Click **Run ▶**.
5. Check Logcat if the application fails to launch.

## 📸 Screenshots

Screenshots will be added to this section as they become available.

Suggested screenshots:

| Screen | Suggested image path |
|---|---|
| Home Screen | `C:\Users\ankit\OneDrive\Pictures\Screenshots\home.png` |
| Product Details | `C:\Users\ankit\OneDrive\Pictures\Screenshots\details.png` |
| AI Shopping Assistant | `C:\Users\ankit\OneDrive\Pictures\Screenshots\AI.png` |
| Price Prediction | `C:\Users\ankit\OneDrive\Pictures\Screenshots\prediction.png` |
| Shopping Cart | `C:\Users\ankit\OneDrive\Pictures\Screenshots\cart.png` |
| Seller Panel | `C:\Users\ankit\OneDrive\Pictures\Screenshots\seller.png` |
| Admin Panel | `C:\Users\ankit\OneDrive\Pictures\Screenshots\admin.png` |
| Profile Screen | `C:\Users\ankit\OneDrive\Pictures\Screenshots\profile.png` |
| Order Screen | `C:\Users\ankit\OneDrive\Pictures\Screenshots\order.png` |
| Place Holder | `C:\Users\ankit\OneDrive\Pictures\Screenshots\orderPlace.png` |
| Sign In | `C:\Users\ankit\OneDrive\Pictures\Screenshots\SignIn.png` |
|Sign Up | `C:\Users\ankit\OneDrive\Pictures\Screenshots\SignUp.png` |

To display an image, upload the screenshot to a `screenshots` folder in the repository and add Markdown such as:

```markdown
![Valmaro Home Screen](C:\Users\ankit\OneDrive\Pictures\Screenshots\home.png)
```

Replace the example paths with the actual screenshot filenames.

## 🗂️ Data Management

Firebase Realtime Database is used for application data such as:

- Products and product details.
- Orders.
- User profile information.
- Cart items.
- Seller details.
- Product stock and inventory.
- Product price history.

The exact database structure and available data depend on the Firebase configuration used by the application.

## 🔐 Access and Security

- User login currently uses a demo-login flow.
- Seller and admin access uses Firebase UID checks.
- Firebase Database security rules should enforce access permissions on the server side.
- API keys, credentials, and other secrets must not be committed to a public repository.
- Client-side role checks alone should not be treated as sufficient security.

## 🛠️ Current Project Status

**Status: Working project with ongoing improvements**

Most major shopping and management features are working. Some bugs and refinements remain.

Areas that may require further testing include:

- AI recommendation quality.
- Accuracy and reliability of price estimates.
- Firebase access permissions.
- Cart, checkout, and order workflows.
- UI consistency and error handling.

## 🔮 Future Scope

Potential improvements include:

- More reliable price forecasting using validated historical data.
- Better AI-based product recommendations.
- Secure backend integration for AI requests.
- Improved product filtering and comparison.
- More robust authentication and role-based access control.
- Enhanced analytics for sellers.
- Automated testing and performance optimization.
- A more polished user interface.

## 🎓 Project Information

- **Project Name:** Valmaro
- **Project Type:** AI-Based E-Commerce Android Application
- **Platform:** Android
- **Primary Language:** Kotlin
- **UI Technology:** XML
- **Database:** Firebase Realtime Database
- **AI Integration:** Google Gemini API
- **Purpose:** College project and portfolio showcase

## 📄 License

No open-source license has currently been selected for this project. Do not assume that the repository grants permission to reuse or redistribute its code.

## 🙌 Acknowledgements

- Android Developers documentation.
- Firebase documentation.
- Google AI for Developers documentation.

---

**Valmaro — Smarter Shopping, Better Decisions.**
