# GeoKids Admin Dashboard

This folder contains the **GeoKids Admin Dashboard** source code.  
The dashboard is built using **React** and is designed **exclusively for administrators** to manage users, drivers, routes, and system data.  

---

## ✨ Features
- 🔑 **Admin authentication** (login/logout)  
- 👥 Manage **users, drivers, and school accounts**  
- 🚌 Assign and monitor **routes**  
- 🔔 Send and manage **alerts/notifications**  
- 📊 View **reports and statistics**  

---

## 🛠️ Tech Stack
- **Frontend**: React.js  
- **State Management**: Context API / Redux (if required)  
- **Styling**: CSS / Tailwind / Material UI (as used in project)  
- **Backend & Services**: Firebase (Authentication, Firestore, Cloud Functions)  

---

## ⚙️ Setup Instructions
1. Navigate to the dashboard folder:  
   ```bash
   cd admin-dashboard
2. Install dependencies:
   ```bash
   npm install
4. Start the development server:
   ```bash
   npm start
6. Open in browser:
   http://localhost:3000

## 📁 Folder Structure
```bash
admin-dashboard/
│── public/            # Static assets
│── src/               # React source code
│   ├── components/    # Reusable UI components
│   ├── pages/         # Dashboard pages (Login, Users, Routes, Reports, etc.)
│   ├── services/      # Firebase or API service helpers
│   ├── hooks/         # Custom React hooks (if used)
│   └── App.js         # App entry point
│
│── package.json
│── README.md

