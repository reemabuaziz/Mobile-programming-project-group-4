<div align="center">
# DIGITAL SHADOW
### Your Digital Footprint, Revealed.
A native Android application that helps users discover, understand, and reduce their digital exposure.
<br>
<img src="https://skillicons.dev/icons?i=kotlin,android,gradle,git,github" height="55"/>
<br><br>
<img src="https://skillicons.dev/icons?i=materialui" height="50"/>
</div>
---
# About
**Digital Shadow** is a privacy-focused Android application designed to help users understand how much of their personal information is exposed across the internet.
The app scans digital footprint indicators, calculates an exposure score, and turns the results into clear actions the user can take to improve their online privacy.
---
# Core Features
- **Digital Footprint Scan**  
  Analyze information associated with the user's online presence.
- **Exposure Score**  
  Convert scan results into a simple overall exposure level.
- **Public Information**  
  Identify information that may be publicly available online.
- **Old Accounts**  
  Detect forgotten or unused online accounts that may require attention.
- **Email Exposure**  
  Identify potentially exposed email addresses using legal and safe data sources.
- **Username Reuse**  
  Check whether usernames appear across multiple online services.
- **Privacy Recommendations**  
  Provide personalized actions based on the scan results.
- **Security Dashboard**  
  Present findings, risks, and recommendations in one clear interface.
---
# Technology Stack
<div align="center">
<img src="https://skillicons.dev/icons?i=kotlin,android,gradle,git,github" height="55"/>
</div>
<br>
| Technology | Role |
|---|---|
| Kotlin | Primary programming language |
| Android | Native mobile platform |
| Jetpack Compose | User interface |
| Material 3 | UI components and design system |
| Android Jetpack | Application architecture and components |
| Gradle | Build system |
| Git | Version control |
| GitHub | Collaboration and source control |
---
# Architecture
```text
                    DIGITAL SHADOW
                          │
                          ▼
                 ┌─────────────────┐
                 │    UI LAYER     │
                 │ Jetpack Compose │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │  DOMAIN LAYER   │
                 │ Business Logic  │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │   DATA LAYER    │
                 │ Data & Sources  │
                 └─────────────────┘

The application follows a layered architecture with Unidirectional Data Flow (UDF) to keep state management and user interactions predictable.

⸻

User Flow

Start
│
▼
User Input
│
▼
Digital Footprint Scan
│
▼
Data Analysis
│
▼
Exposure Score
│
▼
Findings & Risks
│
▼
Privacy Recommendations

⸻

Project Structure

app/
│
├── data/
│   ├── model/
│   ├── repository/
│   └── source/
│
├── domain/
│   ├── model/
│   └── usecase/
│
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
│
├── navigation/
│
└── docs/

⸻

Privacy & Security

Privacy is at the core of Digital Shadow.

The application is designed to work with legal and safe data sources and avoid unnecessary storage of sensitive information.

The goal is to help users understand their digital exposure and take control of their online privacy.

⸻

Development

Digital Shadow is developed collaboratively using Git and GitHub.

Feature
│
▼
Feature Branch
│
▼
Development
│
▼
Commit
│
▼
Pull Request
│
▼
Code Review
│
▼
Merge

⸻

Project Status

In Development

CSC 402 — Mobile Application Programming

Department of Computer Science
Imam Abdulrahman Bin Faisal University

⸻

<div align="center">

DIGITAL SHADOW

See what you leave behind.

<br>
<img src="https://skillicons.dev/icons?i=kotlin,android,git,github" height="45"/>
</div>
```