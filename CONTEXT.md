# Diary App Documentation

## Overview
A multi-language personal diary application that allows users to create, manage, and customize their diary entries with text, images, and emotional states.

## Tech Stack
- **Frontend**: Kotlin, XML layouts
- **Database**: Room (SQLite abstraction)
- **Architecture**: MVVM (Model-View-ViewModel)

## Screen Flow

### 1. Splash Screen
- Displays app branding and loading animation
- Handles initial app setup and data loading
- Duration: 1 seconds

### 2. Language Selection Screen
- **Supported Languages**:
  - English (default)
  - Hindi
  - Spanish
  - French
  - Arabic
  - Bengali
- Persists language preference
- Automatically redirects to Home Screen after selection

### 3. Home Screen
#### Features
- Language switcher
- Diary entries list view
  - Entry title
  - Date
  - Preview text
  - Emotion indicator
  - Image preview
- New entry creation button
- Search and filter capabilities

#### Actions
- Create new entry
- Edit existing entries
- Delete entries
- Change application language

### 4. Create/Edit Screen
#### Input Fields
- Date selector (defaults to current date)
- Title field
- Content area
- Emotion selector (6 options)
- Image attachment section

#### Actions
- Save entry
- Add images
- Navigate back
- Discard changes

### 5. Image Management Screen
#### Features
- Image selection from device gallery
- Multiple image support
- Image preview
- Selection/deselection functionality

#### Permissions
- Camera access
- Storage access
- Runtime permission handling

### 6. Entry Management
#### CRUD Operations
- Create new entries
- Read existing entries
- Update entry content
- Delete entries with confirmation

#### Image Operations
- Add multiple images
- Remove images
- View full-size images
- Image gallery navigation

## Data Management
- Local storage using Room Database
- Image file management
- Language preference persistence
- Entry backup support

## User Experience
- Material Design guidelines
- Intuitive navigation
- Responsive layout
- Dark/Light theme support
- Accessibility features

## Security
- Private data encryption
- Secure image storage
- Optional entry protection 

## Database Schema

### Diary Entry Entity
```kotlin
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val date: Long,  // Timestamp
    val emotion: String,
    val lastModified: Long,
    val isEncrypted: Boolean = false,
    val language: String
)
```

### Image Entity
```kotlin
@Entity(
    tableName = "diary_images",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntry::class,
            parentColumns = ["id"],
            childColumns = ["diaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DiaryImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diaryId: Long,
    val imagePath: String,
    val timestamp: Long,
    val caption: String?
)
```

### Language Preference Entity
```kotlin
@Entity(tableName = "language_preferences")
data class LanguagePreference(
    @PrimaryKey
    val id: Int = 1,  // Single row entity
    val languageCode: String,
    val lastUpdated: Long
)
```

### Database Relationships
- One-to-Many relationship between DiaryEntry and DiaryImage
- Single instance of LanguagePreference for app settings

### Entity Details

#### DiaryEntry
- **id**: Unique identifier for each entry
- **title**: Entry title (max 100 chars)
- **content**: Main diary content
- **date**: Entry date as timestamp
- **emotion**: One of 6 predefined emotions
- **lastModified**: Last modification timestamp
- **isEncrypted**: Flag for encrypted entries
- **language**: Entry's language code

#### DiaryImage
- **id**: Unique identifier for each image
- **diaryId**: Reference to parent diary entry
- **imagePath**: Local storage path
- **timestamp**: Image addition timestamp
- **caption**: Optional image caption

#### LanguagePreference
- **id**: Fixed ID (always 1)
- **languageCode**: Current language selection
- **lastUpdated**: Last change timestamp 