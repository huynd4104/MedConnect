// Scripts for firebase and firebase messaging
importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.6.1/firebase-messaging-compat.js');

// --- !!! SAO CHÉP firebaseConfig TỪ firebase-init.js VÀO ĐÂY !!! ---
const firebaseConfig = {
    apiKey: "AIzaSyDMXxyfkxel7kY8jT70UOzx6rmSaoKLUL4",
    authDomain: "medconnect-1eba7.firebaseapp.com",
    projectId: "medconnect-1eba7",
    storageBucket: "medconnect-1eba7.firebasestorage.app",
    messagingSenderId: "76478248567",
    appId: "1:76478248567:web:2b69f29349e2322b127872",
    measurementId: "G-4WZ0PRHPS3"
};
// ------------------------------------------------------------------

try {
    firebase.initializeApp(firebaseConfig);
    const messaging = firebase.messaging();

    console.log("Firebase SW initialized successfully.");

    // --- Xử lý khi nhận thông báo ở chế độ nền ---
    messaging.onBackgroundMessage((payload) => {
        console.log('[firebase-messaging-sw.js] Received background message ', payload);

        // Tùy chỉnh cách hiển thị thông báo hệ thống
        const notificationTitle = payload.data?.title || 'Thông báo MedConnect';
        const notificationOptions = {
            body: payload.data?.body || '',
            icon: '/images/medconnect-logo.png' // Đường dẫn tới logo (tùy chọn)
            // Bạn có thể thêm các options khác như data, actions,...
        };

        // Hiển thị thông báo bằng Notification API của trình duyệt
        self.registration.showNotification(notificationTitle, notificationOptions);
    });

} catch (e) {
    console.error("Error initializing Firebase SW:", e);
}