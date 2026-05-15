# ShaaleVikas 🏫✨

**ShaaleVikas** (School Development) is a community-driven Android application designed to empower schools by connecting their urgent resource needs with willing contributors. The platform facilitates transparency and tracks the tangible impact of social contributions.

🔗 **Live Demo**: [https://shaale-vikas.vercel.app/](https://shaale-vikas.vercel.app/)


---

## 🚀 Key Features

### 🔐 Secure Authentication
- **Firebase Auth**: Robust login and registration system.
- **Role-Based Access**: Specialized dashboard for Admins to manage school needs and a view-only mode for regular contributors.

### 🎨 Neo-Brutalist Design System
- **Unique Aesthetic**: Custom-built UI components following a modern **Neo-brutalist** style (bold borders, vibrant shadows, and high-contrast colors).
- **Interactive Components**: Custom styled FABs, buttons, and cards for a premium, tactile feel.

- **Request Management**: Admins can post school requirements (e.g., "Library Books", "New Benches", "Repair Roof").
- **Priority Labeling**: Needs are categorized into **High**, **Medium**, and **Low** priority using Material Chips.
- **Cost Estimation**: Clear tracking of estimated funds required for each project.
- **Visual Context**: Support for uploading "Before" photos to provide visual context for the requirement.

### 🌟 Hall of Fame
- A dedicated section to honor and recognize individual and corporate contributors.
- Displays contributor profiles and their history of support.

### 📈 Impact Tracking
- **Before & After**: A visual gallery showing the transformation of school projects after successful contributions.
- **Progress Monitoring**: Keeps contributors updated on how their funds were utilized.

### 💾 Performance & Offline Support
- **Local Persistence**: Integrated `LocalDataManager` to cache data using GSON, ensuring the app remains functional even with intermittent connectivity.
- **Efficient Image Handling**: Local storage optimization for project photos.

---

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Material Design 3](https://m3.material.io/)
- **Backend**: [Firebase](https://firebase.google.com/) (Auth & Firestore)
- **Architecture**: Activity-based with specialized Adapters for high-performance lists.
- **Libraries**:
  - `RecyclerView` & `CardView` for modern list layouts.
  - `Firebase BOM (32.7.0)` for backend services.
  - `Gson` for local data serialization.

---

## ⚙️ Installation & Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Numan-Patil/ShaaleVikas.git
   ```

2. **Firebase Configuration**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with package name `com.project.shaalevikas`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable **Email/Password Authentication** and **Cloud Firestore**.

3. **Build the Project**:
   - Open the project in **Android Studio (Ladybug or newer)**.
   - Sync Gradle files and run the project on an emulator or physical device.

---

## 🏗 Project Structure

```text
app/src/main/java/com/project/shaalevikas/
├── MainActivity.kt        # Login & Entry Point
├── RegisterActivity.kt    # User Registration
├── DashboardActivity.kt   # Core Feed & Admin Actions
├── HallOfFameActivity.kt  # Contributor Recognition
├── ImpactActivity.kt      # Before/After Visuals
├── LocalDataManager.kt    # Offline Caching Logic
├── Need.kt                # Data Model for Requirements
└── *Adapter.kt            # RecyclerView Logic for various feeds
```

---

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

**ShaaleVikas** — Building a better future, one school at a time. 🎓💙
