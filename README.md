# MedConnect - Telemedicine Platform

MedConnect is an online healthcare platform that connects doctors and patients through video calls, appointment scheduling, and medical record management. The project is built on Java Spring Boot and integrates many modern third-party services.

## 🚀 Key Features

* **For Patients:**

* Search for doctors by specialty.

* Schedule appointments.

* Online payment (VNPAY).

* Online video consultation (ZEGOCLOUD).

* Receive appointment notifications via Email and Firebase.

* **For Doctors:**

* Manage work schedules.

* Accept/Reject appointments.

* Write patient records/summaries after consultations.

* **For Admins:**

* Manage users, doctors, and specialties.

* Approval of doctor's records.

## 🛠 Technologies Used

* **Backend:** Java 17, Spring Boot 3.5.6 (Web, Security, Data JPA, Mail, Quartz).

* **Database:** SQL Server (MSSQL).

* **Frontend:** Thymeleaf (Server-side rendering), HTML/CSS/JS.

* **Third-Party Integrations:**

* **ZEGOCLOUD:** Video Call SDK.

* **VNPAY:** Payment gateway.

* **Firebase (FCM):** Push Notification.

* **Google OAuth2:** Sign in with Google.

## ⚙️ Prerequisites

Before installing, ensure your computer has the following installed:

* [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/downloads/#java17)
* [Maven](https://maven.apache.org/) (or use the `mvnw` available in the project)
* [SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) (TCP/IP enabled and SQL Server Authentication enabled).
