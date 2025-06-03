$(document).ready(function() {
    const baseURL = "http://localhost:8081";
    //JWT token is stored in a variable, will be lost when the page is refreshed
    let token = '';
    let userRole = '';

    // Handle login form submission
    $("#loginForm").on("submit", function(e) {
        e.preventDefault();
        const username = $("#username").val();
        const password = $("#password").val();
        $.ajax({
            url: baseURL + "/api/auth/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ username: username, password: password }),
            success: function(response) {
                // Clear any previous error message
                $("#loginError").addClass("d-none").html('');
                token = response.token; //return ResponseEntity.ok(Map.of("token", token));
                userRole = (username.toLowerCase() === 'admin') ? 'ADMIN' : 'ENGINEER';
                $("#loginSection").hide();
                $("#dashboardSection").removeClass("d-none").removeClass("hidden");
                $("#logoutBtn").removeClass("d-none").removeClass("hidden");
                if (userRole === 'ADMIN') {
                    $("#adminActions").removeClass("d-none").removeClass("hidden");
                }
                loadRooms();
            },
            error: function(xhr, status, error) {
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
    $("#createEngineerBtn").on("click", function() {
        $("#createEngineerForm")[0].reset();
        $("#createEngineerModal").modal("show");
    });

    // Handle Create Engineer Account form submission
    $("#createEngineerForm").on("submit", function(e) {
        e.preventDefault();
        const studentData = {
            username: $("#studentUsername").val(),
            email: $("#studentEmail").val(),
            password: $("#studentPassword").val()
        };
        $.ajax({
            url: baseURL + "/api/performance/engineers",
            method: "POST",
            contentType: "application/json",
            headers: { "Authorization": "Bearer " + token },
            data: JSON.stringify(studentData),
            success: function(response) {
                $(document.activeElement).blur();
                $("#createStudentModal").modal("hide");
                setTimeout(function() {
                    showAlert(response.message, "success");
                }, 500);
            },
            error: function() {
                showAlert("Failed to create ngineer account", "danger");
            }
        });
    });

    // Logout button: Show logout confirmation modal
    $("#logoutBtn").on("click", function() {
        $("#logoutModal").modal("show");
    });

    // Confirm Logout: Clear token, reset userRole, destroy DataTable, and return to login page.
    $("#confirmLogoutBtn").on("click", function() {
        token = '';
        userRole = '';
        if (roomsTable) {
            roomsTable.destroy();
        }
        $("#dashboardSection").addClass("hidden");
        $("#logoutBtn").addClass("hidden");
        $("#adminActions").addClass("hidden");
        $("#loginSection").show();
        $("#logoutModal").modal("hide");
    });
});