<h1 align="center">🏥 Hospital Management System</h1>

<p align="center">
  <b>Java Desktop Application using Swing, JDBC, Socket Programming & RMI</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange" />
  <img src="https://img.shields.io/badge/GUI-Swing-blue" />
  <img src="https://img.shields.io/badge/Database-JDBC-green" />
  <img src="https://img.shields.io/badge/Networking-Socket%20%2B%20RMI-red" />
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen" />
</p>

---

<h2>📌 Overview</h2>

<p>
This project is a <b>Hospital Management System</b> built as a desktop application.
It manages hospital operations such as patient registration, doctor management,
appointments, and payments using role-based access control.
</p>

---

<h2>🚀 Features</h2>

<h3>🔐 Authentication</h3>
<ul>
  <li>Secure login system</li>
  <li>Role-based access (Admin, Doctor, Receptionist, Patient)</li>
  <li>First-time password change</li>
</ul>

<h3>👨‍💼 Admin</h3>
<ul>
  <li>Manage doctors</li>
  <li>Manage users</li>
  <li>View system data</li>
</ul>

<h3>🧑‍💼 Receptionist</h3>
<ul>
  <li>Add patients</li>
  <li>Book appointments</li>
  <li>Process payments</li>
</ul>

<h3>👨‍⚕️ Doctor</h3>
<ul>
  <li>View patients</li>
  <li>View appointments</li>
  <li>Add prescriptions</li>
</ul>

<h3>🧑 Patient</h3>
<ul>
  <li>View appointments</li>
  <li>View prescriptions</li>
  <li>View payment history</li>
</ul>

---

<h2>🧱 Architecture</h2>

<p align="center">
UI Layer (Swing) <br>
↓ <br>
Service Layer (Auth, RMI, Networking) <br>
↓ <br>
Database Layer (JDBC)
</p>

---

<h2>🌐 Networking</h2>

<h3>🔹 Socket Programming</h3>
<ul>
  <li>Client-server communication</li>
  <li>ClientHelper.java → sends messages</li>
  <li>Server.java → handles requests</li>
</ul>

<h3>🔹 RMI (Remote Method Invocation)</h3>
<ul>
  <li>Remote method calls between client and server</li>
  <li>HospitalService.java → Interface</li>
  <li>HospitalServiceImpl.java → Implementation</li>
  <li>RMIServer.java → Server</li>
  <li>RMIClient.java → Client</li>
</ul>

---

<h2>🗂️ Project Structure</h2>

<pre>
project/
│
├── ui/
├── auth/
├── database/
├── networking/
├── rmi/
├── util/
</pre>

---

<h2>⚙️ Technologies Used</h2>

<ul>
  <li>Java (Swing)</li>
  <li>JDBC</li>
  <li>MySQL (or any relational DB)</li>
  <li>Java RMI</li>
  <li>Socket Programming</li>
</ul>

---

<h2>▶️ How to Run</h2>

<pre>
java rmi.RMIServer
java networking.Server
java ui.Login
</pre>

---

<h2>🧪 Demo Flow</h2>

<ol>
  <li>Login</li>
  <li>Open dashboard</li>
  <li>Perform actions</li>
  <li>Data stored in database</li>
</ol>

---

<h2>🔐 Security</h2>

<ul>
  <li>Input validation</li>
  <li>Password change on first login</li>
  <li>Role-based access control</li>
</ul>

---

<h2>📄 Utilities</h2>

<ul>
  <li>CSV Export</li>
  <li>Receipt Generator</li>
  <li>File Logging</li>
</ul>

---

<h2>👨‍💻 Authors</h2>

<p>
ATSEE Group:
<br>Abenezer Samson
<br>Tsion Asrat
<br>Sumeya Ahmed
<br>Enas Remedan
<br>Eyerusalem Berihun
</p>

---

<h2>⭐ Conclusion</h2>

<p>
This project demonstrates desktop application development, database integration,
networking (Socket + RMI), and role-based system design.
</p>
