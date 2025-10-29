const firebaseConfig = {
    apiKey: "AIzaSyDMXxyfkxel7kY8jT70UOzx6rmSaoKLUL4",
    authDomain: "medconnect-1eba7.firebaseapp.com",
    projectId: "medconnect-1eba7",
    storageBucket: "medconnect-1eba7.firebasestorage.app",
    messagingSenderId: "76478248567",
    appId: "1:76478248567:web:2b69f29349e2322b127872",
    measurementId: "G-4WZ0PRHPS3"
};

const VAPID_KEY = "BB4IrqfAm1mx7cdquaATWFBSLQH13HvN0TenAP0MYOJOD1xYegRHMjjMlqFU6JlgoE393FFdb4V4VpwrbdI8-IQ";

let firebaseApp;
let messaging;

document.addEventListener('DOMContentLoaded', () => {

    try {
        firebaseApp = firebase.initializeApp(firebaseConfig);
        console.log("Firebase initialized successfully.");

        messaging = firebase.messaging();
        console.log("Firebase Messaging initialized.");

        function requestPermissionAndGetToken() {
            console.log('Requesting permission...');
            Notification.requestPermission().then((permission) => {
                if (permission === 'granted') {
                    console.log('Notification permission granted.');
                    const enableButton = document.getElementById('enable-notifications-btn');
                    if (enableButton) {
                        enableButton.style.display = 'none';
                    }
                    messaging.getToken({ vapidKey: VAPID_KEY })
                        .then((currentToken) => {
                            if (currentToken) {
                                console.log('FCM Token:', currentToken);
                                sendTokenToServer(currentToken);
                                localStorage.setItem('fcm_token', currentToken);
                            } else {
                                console.log('No registration token available. Request permission to generate one.');
                                if (enableButton) {
                                    enableButton.classList.remove('hidden');
                                    enableButton.style.display = 'inline-flex';
                                }
                            }
                        }).catch((err) => {
                        console.log('An error occurred while retrieving token. ', err);
                        if (enableButton) {
                            enableButton.classList.remove('hidden');
                            enableButton.style.display = 'inline-flex';
                        }
                    });
                } else {
                    console.log('Unable to get permission to notify.');
                    const enableButton = document.getElementById('enable-notifications-btn');
                    if (enableButton) {
                        enableButton.classList.remove('hidden');
                        enableButton.style.display = 'inline-flex';
                    }
                }
            });
        }

        function sendTokenToServer(token) {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            const headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            fetch('/api/users/update-fcm-token', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ token: token })
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Token sent to server successfully.');
                    } else {
                        console.error('Failed to send token to server. Status:', response.status);
                        response.text().then(text => console.error('Server response:', text));
                    }
                })
                .catch(error => {
                    console.error('Error sending token to server:', error);
                });
        }

        messaging.onMessage((payload) => {
            console.log('Foreground message received. ', payload);

            const notificationTitle = payload.data?.title || 'Thông báo mới';
            const notificationBody = payload.data?.body || '';

            showForegroundNotification(notificationTitle, notificationBody);

            setTimeout(() => {
                if (window.medconnect && typeof window.medconnect.fetchNotifications === 'function') {
                    console.log("Calling window.medconnect.fetchNotifications after delay...");
                    window.medconnect.fetchNotifications();
                } else {
                    console.error("window.medconnect.fetchNotifications still not found after delay!");
                    // Chỉ cập nhật badge như fallback cuối cùng
                    const badgeElement = document.getElementById('notification-count');
                    if (badgeElement) {
                        let currentCount = parseInt(badgeElement.textContent || '0');
                        currentCount++;
                        badgeElement.textContent = currentCount;
                        badgeElement.classList.remove('hidden');
                        console.warn("Updated badge count as fallback.");
                    }
                }
            }, 500); // Đợi 500ms, bạn có thể điều chỉnh
        });

        function checkNotificationStatus() {
            const storedToken = localStorage.getItem('fcm_token');
            const enableButton = document.getElementById('enable-notifications-btn');

            if ('Notification' in window) {
                if (Notification.permission === 'granted') {
                    console.log("Notification permission already granted.");
                    if (enableButton) enableButton.classList.add('hidden');
                    if (!storedToken) {
                        console.log("Permission granted but no token found, requesting token...");
                        requestPermissionAndGetToken();
                    } else {
                        console.log("Permission granted, token exists:", storedToken);
                        sendTokenToServer(storedToken);
                    }
                } else if (Notification.permission === 'denied') {
                    console.log("Notification permission denied.");
                    if (enableButton) {
                        enableButton.textContent = "Thông báo bị chặn";
                        enableButton.disabled = true;
                        enableButton.classList.remove('hidden');
                        enableButton.style.display = 'inline-flex';
                        enableButton.classList.replace('bg-blue-500','bg-gray-400');
                        enableButton.classList.replace('hover:bg-blue-600','hover:bg-gray-400');
                    }
                } else {
                    console.log("Notification permission not yet requested or dismissed.");
                    if (enableButton) {
                        enableButton.classList.remove('hidden');
                        enableButton.style.display = 'inline-flex';
                    }
                }
            } else {
                console.log("This browser does not support desktop notification");
                if (enableButton) enableButton.style.display = 'none';
            }
        }

        checkNotificationStatus();

        const enableButton = document.getElementById('enable-notifications-btn');
        if (enableButton) {
            enableButton.addEventListener('click', requestPermissionAndGetToken);
        }

    } catch (e) {
        console.error("Error initializing Firebase or Messaging:", e);
        const enableButton = document.getElementById('enable-notifications-btn');
        if (enableButton) enableButton.style.display = 'none';
    }

});

function showForegroundNotification(title, body) {
    const notificationDiv = document.createElement('div');
    notificationDiv.style.position = 'fixed';
    notificationDiv.style.top = '80px';
    notificationDiv.style.right = '20px';
    notificationDiv.style.padding = '15px';
    notificationDiv.style.backgroundColor = '#2563eb';
    notificationDiv.style.color = 'white';
    notificationDiv.style.borderRadius = '8px';
    notificationDiv.style.zIndex = '1001';
    notificationDiv.style.boxShadow = '0 4px 15px rgba(0,0,0,0.2)';
    notificationDiv.style.maxWidth = '300px';
    notificationDiv.innerHTML = `<strong style="font-size: 1.1em; display: block; margin-bottom: 5px;">${title}</strong><span style="font-size: 0.95em;">${body}</span>`;

    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '&times;';
    closeBtn.style.position = 'absolute';
    closeBtn.style.top = '5px';
    closeBtn.style.right = '10px';
    closeBtn.style.background = 'none';
    closeBtn.style.border = 'none';
    closeBtn.style.color = 'white';
    closeBtn.style.fontSize = '1.5em';
    closeBtn.style.cursor = 'pointer';
    closeBtn.onclick = () => notificationDiv.remove();
    notificationDiv.appendChild(closeBtn);


    document.body.appendChild(notificationDiv);

    setTimeout(() => {
        if (document.body.contains(notificationDiv)) {
            notificationDiv.remove();
        }
    }, 7000);
}
