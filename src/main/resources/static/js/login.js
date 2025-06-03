$(document).ready(function() {
    const baseURL = "http://localhost:8081";
    const token = localStorage.getItem('authToken');
    let userRole = localStorage.getItem('userRole');

    $('#loginSection').removeClass("d-none");
    $('#dashboardSection').addClass("d-none");
    $('.layout').addClass("d-none")
    $('.nav-items').addClass("d-none")
    $('#logoutBtn').addClass("d-none");
    $('#createEngineerSpan').addClass("d-none");


    // If token exists (page reload), restore AJAX headers & UI
    if (token) {
        $.ajaxSetup({
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        $('#loginSection').addClass("d-none");
        $('#dashboardSection').removeClass("d-none");
        $('.layout').removeClass("d-none")
        $('.nav-items').removeClass("d-none")
        console.log("✅ Nav items shown");
        $('#logoutBtn').removeClass("d-none");
        console.log("✅ Logout button shown");
        $('#createEngineerSpan').toggleClass("d-none", userRole !== 'ROLE_ADMIN');

        // Initialize dashboard and connection check
        window.initializeDashboard();
        window.updateConnectionStatus();
    }

    // Handle login form submission
    $("#loginForm").on("submit", function(e) {
        e.preventDefault();
        const username = $("#username").val().trim();
        const password = $("#password").val().trim();

        if (!username || !password) {
            showAlert("Username and Password are required.");
            return;
        }

        $.ajax({
            url: baseURL + "/api/auth/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ username: username, password: password }),
            success: function(response) {
                console.log("Login response:", response);

                // Clear any previous error message
                $("#loginError").addClass("d-none").html('');

                localStorage.setItem('authToken', response.token);
                localStorage.setItem('userRole', response.role); // store role if returned
                userRole = response.role; // Update variable here

                // Make sure all future AJAX calls use the token
                $.ajaxSetup({
                    headers: {
                        'Authorization': 'Bearer ' + response.token
                    }
                });

                $("#loginSection").addClass("d-none");
                // dashboard hidden until user logs in
                $("#dashboardSection").removeClass("d-none");
                console.log("✅ dashboardSection shown");
                $('.layout').removeClass("d-none")
                $('.nav-items').removeClass("d-none")
                console.log("✅ Nav items shown");
                $("#logoutBtn").removeClass("d-none");

                console.log("userRole: ", userRole); // not printed
                $('#createEngineerSpan').toggleClass("d-none", userRole !== 'ROLE_ADMIN');

                window.initializeDashboard();
                // ✅ Then start connection check
                window.updateConnectionStatus();  // Now this runs AFTER user is authenticated

            },
            error: function(xhr) {
                let errorMessage = "Invalid credentials. Please try again.";
                // Try to parse JSON error message if available
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (response.error) {
                        errorMessage = response.error;
                    }
                } catch (e) {
                    // If not JSON, fallback to the default message.
                }
                $("#loginError").removeClass("d-none").html(errorMessage);
            }
        });
    });

    function showAlert(message, type) {
        const alertHTML = `
	      <div class="alert alert-${type} alert-dismissible fade show" role="alert">
	        ${message}
	        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
	          <span aria-hidden="true">&times;</span>
	        </button>
	      </div>
	    `;
        $("#alertPlaceholder").html(alertHTML);

        // Auto-dismiss the alert after 3 seconds.
        setTimeout(() => {
            $(".alert").alert('close');
        }, 3000);
    }

    // Show Create Engineer Account Modal (admin only)
    $("#createEngineerSpan").on("click", function () {
        const modal = $("#createEngineerModal");

        if (modal.length) {
            $("#createEngineerForm")[0].reset();
            modal.modal("show");
        } else {
            console.warn("#createEngineerModal not found in DOM");
        }
    });

    // Handle Create Engineer Account form submission
    $("#createEngineerForm").on("submit", function(e) {
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
                $(document.activeElement).blur();
                $("#createEngineerModal").modal("hide");
                setTimeout(function() {
                    showAlert(response.message, "success");
                }, 500);
            },
            error: function() {
                showAlert("Failed to create engineer account", "danger");
            }
        });
    });

    // Logout button: Show logout confirmation modal
    $("#logoutBtn").on("click", function() {
        $("#logoutModal").modal("show");
    });

    // Confirm logout
    $(document).on("click", "#confirmLogoutBtn", function() {
        console.log("✅ Logout confirmed");

        localStorage.removeItem("authToken");
        localStorage.removeItem("userRole");
        sessionStorage.setItem("logoutMessage", "✔ You have successfully logged out!");

        // Blur the focused button to avoid aria-hidden issue
        this.blur();
        $("#logoutModal").modal("hide");

        // Clean up dashboard
        window.shutdownDashboard();

        $("#loginSection").removeClass("d-none");
        $("#dashboardSection").addClass("d-none");
        $(".layout, .nav-items").addClass("d-none");
        $("#logoutBtn, #createEngineerSpan").addClass("d-none");

        $("#loginForm")[0].reset();
        $("#loginError").addClass("d-none").html("");

        $.ajaxSetup({ headers: { Authorization: "" } });

        showAlert("Successfully logged out!", "success");
    });
});