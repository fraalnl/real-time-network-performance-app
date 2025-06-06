$(document).ready(function() {
    const baseURL = "http://localhost:8081";
    const token = localStorage.getItem('authToken');
    let userRole = localStorage.getItem('userRole');
    let dashboardInitialized = false;

    // ALWAYS start with login section visible - ISSUE 2 FIX
    showLoginSection();

    // Check for logout success message
    const logoutMessage = sessionStorage.getItem('logoutMessage');
    if (logoutMessage) {
        showNotification(logoutMessage, "success");
        sessionStorage.removeItem('logoutMessage');
    }

    // Only validate token if it exists AND we're not showing logout message
    if (token && !logoutMessage) {
        console.log("🔑 Token found, validating...");
        validateTokenAndShowDashboard();
    } else {
        console.log("🔐 No valid token found, showing login");
    }

    // Handle login form submission
    $("#loginForm").on("submit", function(e) {
        e.preventDefault();
        const username = $("#username").val().trim();
        const password = $("#password").val().trim();

        if (!username || !password) {
            showError("Username and Password are required.");
            return;
        }

        performLogin(username, password);
    });

    // Create Engineer Modal Events
    $("#createEngineerSpan").on("click", function() {
        if (userRole === 'ROLE_ADMIN') {
            $("#createEngineerForm")[0].reset();
            $("#createEngineerModal").css('display', 'flex');
        }
    });

    $("#closeCreateModal, #cancelCreateBtn").on("click", function() {
        $("#createEngineerModal").css('display', 'none');
    });

    $("#createEngineerForm").on("submit", function(e) {
        handleCreateEngineer(e);
    });

    // Logout Modal Events
    $("#logoutBtn").on("click", function() {
        $("#logoutModal").css('display', 'flex');
    });

    $("#closeLogoutModal, #cancelLogoutBtn").on("click", function() {
        $("#logoutModal").css('display', 'none');
    });

    $("#confirmLogoutBtn").on("click", function() {
        performLogout();
    });

    // Close modals when clicking outside
    $("#createEngineerModal, #logoutModal").on("click", function(e) {
        if (e.target === this) {
            $(this).css('display', 'none');
        }
    });

    function validateTokenAndShowDashboard() {
        // Add small delay to ensure UI is ready
        setTimeout(() => {
            $.ajax({
                url: baseURL + "/api/metrics/summary",
                method: "HEAD",
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                timeout: 5000, // Add timeout
                success: function() {
                    console.log("✅ Token valid, showing dashboard");

                    // Set AJAX defaults
                    $.ajaxSetup({
                        headers: {
                            'Authorization': 'Bearer ' + token
                        }
                    });

                    showDashboardSection();
                },
                error: function(xhr) {
                    console.log("❌ Token invalid (Status: " + xhr.status + "), clearing storage");
                    clearAuth();
                    showLoginSection();
                }
            });
        }, 100); // Small delay to ensure proper initialization
    }

    function performLogin(username, password) {
        $.ajax({
            url: baseURL + "/api/auth/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ username: username, password: password }),
            success: function(response) {
                console.log("✅ Login successful:", response);

                // Clear any previous error message
                hideError();

                // Store auth data
                localStorage.setItem('authToken', response.token);
                localStorage.setItem('userRole', response.role);
                userRole = response.role;

                // Set default AJAX headers for future requests
                $.ajaxSetup({
                    headers: {
                        'Authorization': 'Bearer ' + response.token
                    }
                });

                // Show dashboard
                showDashboardSection();

                // Show success notification
                showNotification("Welcome! Successfully logged in.", "success");
            },
            error: function(xhr) {
                let errorMessage = "Invalid credentials. Please try again.";
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (response.error) {
                        errorMessage = response.error;
                    }
                } catch (e) {
                    // Use default message if JSON parsing fails
                }
                showError(errorMessage);
            }
        });
    }

    function handleCreateEngineer(e) {
        e.preventDefault();

        const engineerData = {
            username: $("#engineerUsername").val(),
            password: $("#engineerPassword").val()
        };

        $.ajax({
            url: baseURL + "/api/performance/engineers",
            method: "POST",
            contentType: "application/json",
            headers: { "Authorization": "Bearer " + localStorage.getItem('authToken') },
            data: JSON.stringify(engineerData),
            success: function(response) {
                $("#createEngineerModal").css('display', 'none');
                showNotification(response.message || "Engineer account created successfully!", "success");
            },
            error: function(xhr) {
                let errorMessage = "Failed to create engineer account";
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (response.error) {
                        errorMessage = response.error;
                    }
                } catch (e) {
                    // Use default message
                }
                showNotification(errorMessage, "error");
            }
        });
    }

    function performLogout() {
        console.log("🚪 Performing logout...");

        // First cleanup dashboard - ISSUE 3 FIX
        if (window.cleanupDashboard) {
            window.cleanupDashboard();
        }

        // Clear auth data
        clearAuth();

        // Hide modal and show login
        $("#logoutModal").css('display', 'none');
        showLoginSection();

        // Clear AJAX default headers
        $.ajaxSetup({
            headers: {}
        });

        // Mark dashboard as not initialized
        dashboardInitialized = false;

        // ISSUE 1 FIX: Store logout message for next page load
        sessionStorage.setItem("logoutMessage", "✅ You have successfully logged out!");

        // Redirect to ensure clean state
        window.location.href = "/index.html";
    }

    function showLoginSection() {
        console.log("👤 Showing login section");
        $("#loginSection").css('display', 'flex');
        $("#dashboardSection").css('display', 'none');
        $(".navbar").css('display', 'none');

        // ISSUE 2 & 3 FIX: Ensure dashboard is fully stopped
        if (window.cleanupDashboard && dashboardInitialized) {
            window.cleanupDashboard();
            dashboardInitialized = false;
        }
    }

    function showDashboardSection() {
        console.log("📊 Showing dashboard section");
        $("#loginSection").css('display', 'none');
        $("#dashboardSection").css('display', 'flex');
        $(".navbar").css('display', 'flex');

        // Update UI based on role
        if (userRole === 'ROLE_ADMIN') {
            $("#createEngineerSpan").show();
        } else {
            $("#createEngineerSpan").hide();
        }

        // Initialize dashboard only once
        if (!dashboardInitialized && window.initializeDashboard) {
            console.log("🎯 Initializing dashboard for first time");
            window.initializeDashboard();
            dashboardInitialized = true;
        }

        // Update connection status
        if (window.updateConnectionStatus) {
            window.updateConnectionStatus();
        }
    }

    function clearAuth() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userRole');
        userRole = null;
    }

    function showError(message) {
        $("#loginError").text(message).show();
    }

    function hideError() {
        $("#loginError").hide();
    }

    function showNotification(message, type = "success") {
        let container = $("#notificationsContainer");

        // Create container if it doesn't exist
        if (container.length === 0) {
            container = $('<div id="notificationsContainer"></div>');
            $('body').append(container);
        }

        const icon = type === "success" ? "✅" : "❌";

        const notification = $(`
            <div class="notification ${type}">
                <div class="notification-content">
                    <span class="notification-icon">${icon}</span>
                    <span class="notification-message">${message}</span>
                    <button class="notification-close">&times;</button>
                </div>
            </div>
        `);

        // Add close functionality
        notification.find('.notification-close').on('click', function() {
            notification.remove();
        });

        container.append(notification);

        // Auto-remove after 5 seconds
        setTimeout(function() {
            notification.remove();
        }, 5000);
    }
});