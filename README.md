<div align="center">

DIGITAL SHADOW | الظل الرقمي

Discover your digital footprint.

تطبيق أندرويد يساعد المستخدم على اكتشاف وفهم بصمته الرقمية ومعرفة المعلومات والحسابات والبيانات التي قد تكون مرتبطة به على الإنترنت.

A native Android application that helps users discover and understand their digital footprint, identify potentially exposed information and accounts, and improve their online privacy.

<br>
<img src="https://skillicons.dev/icons?i=kotlin,android,git,github" />
<img src="https://skillicons.dev/icons?i=compose" />
</div>

⸻

About the Project | عن المشروع

Digital Shadow | الظل الرقمي is a privacy-focused mobile application designed to help users understand how much personal information they have left across the internet.

يهدف التطبيق إلى زيادة وعي المستخدم ببصمته الرقمية من خلال تحليل مجموعة من المؤشرات المرتبطة بوجود معلوماته وحساباته على الإنترنت، ثم عرض النتائج بطريقة بسيطة وواضحة.

The application provides a digital privacy scan that helps users identify areas that may require attention and provides recommendations for improving their online privacy.

⸻

Core Features | المميزات الرئيسية

Digital Footprint Scan

Analyze the user’s digital footprint and identify potentially exposed information across online services.

Exposure Score

Generate an overall Digital Exposure Score that gives the user a clear understanding of their current level of online exposure.

Public Information

Identify publicly available information that may contribute to the user’s digital footprint.

Old Accounts

Highlight old or unused accounts that may no longer be needed.

Exposed Emails

Identify email addresses that may have appeared in publicly available or legally accessible data sources.

Reused Usernames

Detect usernames that may be reused across different online services.

Privacy Recommendations

Provide practical recommendations based on the scan results to help users improve their digital privacy.

Exposure Dashboard

Present the results of the scan through a clear and easy-to-understand dashboard.

⸻

Digital Exposure | التعرض الرقمي

The application summarizes scan results into actionable insights.

Example results may include:

* High digital exposure
* Accounts that should be secured
* Old accounts that should be reviewed or removed
* Services with unnecessary permissions
* Publicly exposed information that requires attention

The goal is not only to show the user what is exposed, but also to help them understand what they can do about it.

⸻

Technology Stack | التقنيات المستخدمة

<div align="center">

Technology	Purpose
Kotlin	Primary programming language
Android	Native mobile platform
Jetpack Compose	Modern UI development
Git	Version control
GitHub	Team collaboration and project management

</div>
<br>
<div align="center">
<img src="https://skillicons.dev/icons?i=kotlin,android,git,github" />
</div>

⸻

Architecture | المعمارية

The application follows a layered architecture to keep the project organized, maintainable, and scalable.

┌─────────────────────────────┐
│           UI Layer          │
│     Jetpack Compose UI      │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│        Domain Layer         │
│    Application Logic        │
└──────────────┬──────────────┘
│
▼
┌─────────────────────────────┐
│          Data Layer         │
│ Data Sources & Repositories │
└─────────────────────────────┘

The project also follows Unidirectional Data Flow (UDF) to keep application state and user interactions predictable.

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

Privacy & Security | الخصوصية والأمان

Privacy is a core principle of Digital Shadow.

The application is designed to use legal and safe data sources when analyzing publicly available information.

The application will avoid unnecessarily storing highly sensitive information such as passwords or authentication credentials.

The purpose of the application is to educate users about their digital footprint and help them improve their privacy, not to collect or expose sensitive personal data.

⸻

User Flow | رحلة المستخدم

Start
│
▼
Enter / Select Information
│
▼
Digital Footprint Scan
│
▼
Analyze Results
│
▼
Calculate Exposure Score
│
▼
Display Findings
│
▼
Privacy Recommendations
│
▼
Improve Digital Privacy

⸻

Project Goals | أهداف المشروع

* Increase awareness of digital footprints.
* Help users understand what information may be exposed online.
* Identify old or unnecessary online accounts.
* Provide a clear digital exposure assessment.
* Give users practical privacy recommendations.
* Present complex privacy information in a simple mobile experience.

⸻

Development Workflow | سير العمل

The project is developed collaboratively using Git and GitHub.

Each feature is developed in its own branch, reviewed through Pull Requests, and merged into the main branch after review.

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
Review
│
▼
Merge

⸻

Team

CSC 402 — Mobile Application Programming

Department of Computer Science
Imam Abdulrahman Bin Faisal University

⸻

Project Status

In Development

Digital Shadow is currently under development as part of the CSC 402 Mobile Application Programming Term Project.

⸻

<div align="center">

DIGITAL SHADOW

Understand your footprint.
Protect your privacy.

</div>