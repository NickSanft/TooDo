# TooDo

TooDo is a mostly vibe-coded, gamified task management Android application designed to help users stay motivated by rewarding them for completing their daily tasks.

## Features

- **Task Management**: Create, edit, and delete tasks.
- **Prioritization**: Assign priority levels (High, Medium, Low) to tasks to focus on what matters most.
- **Gamification**: Earn points for completing tasks based on their difficulty.
- **Rewards System**: Redeem earned points for custom prizes (e.g., "Movie Night", "Ice Cream").
- **History**: View completed tasks and a ledger of your point history.
- **Settings**: Customize application behavior.

## How to Run

1.  **Prerequisites**:
    *   Android Studio (latest version recommended)
    *   JDK 17 or higher
    *   Android SDK

2.  **Installation**:
    *   Clone the repository or download the source code.
    *   Open Android Studio and select "Open an existing project".
    *   Navigate to the project directory (`.../TooDo`) and click OK.
    *   Wait for Gradle sync to complete.

3.  **Running on a Device/Emulator**:
    *   Connect an Android device via USB or create an Android Virtual Device (AVD) using the AVD Manager.
    *   Click the "Run" button (green play icon) in the toolbar or press `Shift + F10`.

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room
- **UI**: XML Layouts, Material Design Components, ViewBinding
- **Testing**: Espresso, UI Automator
