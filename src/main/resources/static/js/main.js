// main.js
// Main entry point that wires up all user story modules

import { startRealTimeKPIUpdates, pauseUpdates, resumeUpdates, isPausedState } from './realTimeMetrics.js';import { AlertsManager } from './alertsManager.js';
import { NodesView } from './nodesView.js';

let currentView = 'dashboard';

document.addEventListener('DOMContentLoaded', () => {
    console.log("🚀 Main.js loaded. Waiting for login...");

    const token = localStorage.getItem("authToken");
    if (token) {
        $.ajaxSetup({
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
    }
});

function initializeDashboard() {
    // 1. Start real-time KPI updates (User Story 1)
    startRealTimeKPIUpdates();

    // 2. Initialize alerts manager
    const alertsManager = new AlertsManager();
    alertsManager.initialize();

    // 3. Initialize nodes view and make it globally available
    window.nodesView = new NodesView();
    window.nodesView.initialize();

    // 4. Setup control buttons
    setupControlButtons();

    // 5. Initialize connection status
    // updateConnectionStatus();

    // 6. Setup navigation handlers
    setupNavigation();



    console.log("✅ Dashboard initialization complete");
}

window.initializeDashboard = initializeDashboard; // Make it globally visible
window.updateConnectionStatus = updateConnectionStatus;

function setupControlButtons() {
    const pauseBtn = document.getElementById('pauseBtn');
    const refreshBtn = document.getElementById('refreshBtn');

    if (pauseBtn) {
        pauseBtn.addEventListener('click', () => {
            togglePause();
        });
    }

    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            forceRefresh();
        });
    }
}

function togglePause() {
    const currentlyPaused = isPausedState();
    const pauseBtn = document.getElementById('pauseBtn');

    if (currentlyPaused) {
        resumeUpdates();
        pauseBtn.innerHTML = '⏸️ Pause';
        pauseBtn.title = 'Pause real-time updates';

        const statusElement = document.getElementById('connectionStatus');
        if (statusElement && !statusElement.textContent.includes('Demo')) {
            statusElement.innerText = '🟢 Connected';
            statusElement.className = 'connection-status status-connected';
        }
    } else {
        pauseUpdates();
        pauseBtn.innerHTML = '▶️ Resume';
        pauseBtn.title = 'Resume real-time updates';

        const statusElement = document.getElementById('connectionStatus');
        if (statusElement) {
            statusElement.innerText = '⏸️ Paused';
            statusElement.className = 'connection-status status-paused';
        }
    }
}

function forceRefresh() {
    console.log('🔄 Manual refresh triggered');

    // Call the refresh function from realTimeMetrics.js if available
    if (window.updateKPIData && !isPaused) {
        window.updateKPIData();
    }

    // Visual feedback
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.innerHTML = '🔄 Refreshing...';
        setTimeout(() => {
            refreshBtn.innerHTML = '🔄 Refresh';
        }, 1000);
    }
}

function updateConnectionStatus() {
    const statusElement = document.getElementById('connectionStatus');
    if (statusElement) {
        statusElement.innerText = '🟡 Initializing...';
        statusElement.className = 'connection-status status-initializing';

        // Check connection after a short delay
        setTimeout(checkAPIConnection, 2000);
    }
}

async function checkAPIConnection() {
    const statusElement = document.getElementById('connectionStatus');
    if (!statusElement) return;

    try {
        // Try to reach your Spring Boot API
        const response = await fetch('http://localhost:8081/api/metrics/summary', {
            method: 'HEAD', // Just check if endpoint exists
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            timeout: 5000
        });

        if (response.ok) {
            statusElement.innerText = '🟢 Connected';
            statusElement.className = 'connection-status status-connected';
            console.log('✅ API connection established');
        } else {
            throw new Error('API not responding');
        }
    } catch (error) {
        statusElement.innerText = '🟡 Demo Mode';
        statusElement.className = 'connection-status status-demo';
        console.log('🟡 Using demo mode - API unavailable');
    }
}

function setupNavigation() {
    // Add click handlers for sidebar navigation
    const navItems = document.querySelectorAll('.sidebar li');

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            console.log('🔗 Sidebar navigation clicked:', e.target.textContent);

            // Remove active class from all items
            navItems.forEach(nav => nav.classList.remove('active'));

            // Add active class to clicked item
            e.target.classList.add('active');

            // Get the navigation text and clean it (remove badges/numbers)
            const navText = e.target.textContent.trim();
            const cleanNavText = navText.replace(/\s*\d+\s*$/, '').trim(); // Remove numbers at end

            console.log('🧭 Cleaned navigation text:', cleanNavText);
            handleNavigation(cleanNavText);
        });
    });

    // Add click handlers for top navbar - these should also work for Dashboard
    const topNavItems = document.querySelectorAll('.navbar .nav-items span:not(.connection-status)');

    topNavItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const navText = e.target.textContent.trim();
            console.log(`🔗 Top Navigation clicked: ${navText}`);

            // Handle top navigation
            if (navText === 'Dashboard') {
                // Remove active from sidebar items
                document.querySelectorAll('.sidebar li').forEach(nav => nav.classList.remove('active'));
                // Add active to Dashboard in sidebar
                const dashboardItem = document.querySelector('.sidebar li:first-child');
                if (dashboardItem) dashboardItem.classList.add('active');

                handleNavigation('Dashboard');
            }
        });
    });

    console.log('🧭 Navigation setup complete');
}

function handleNavigation(section) {
    console.log(`📍 Navigating to: "${section}"`);
    console.log('🔍 Available nodesView:', !!window.nodesView);

    // Clean the section text (remove badge numbers and extra whitespace)
    const cleanSection = section.toLowerCase().trim();
    currentView = cleanSection;

    console.log(`🎯 Clean section: "${cleanSection}"`);

    switch (cleanSection) {
        case 'dashboard':
            showDashboardView();
            break;
        case 'nodes':
            showNodesView();
            break;

        default:
            console.log(`❓ Unknown navigation: "${section}" -> "${cleanSection}"`);
            // Default to dashboard if unknown
            showDashboardView();
    }

    function showDashboardView() {
        console.log('📊 Activating Dashboard view');

        // Show dashboard elements
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');

        if (kpiCards) {
            kpiCards.style.display = 'grid';
            console.log('✅ KPI cards shown');
        }
        if (chartContainer) {
            chartContainer.style.display = 'block';
            console.log('✅ Chart container shown');
        }

        // Hide nodes view
        if (window.nodesView) {
            window.nodesView.hideNodesView();
            console.log('✅ Nodes view hidden');
        }
    }



// ✅ NEW (actual implementation) - What you need
    function showNodesView() {
        console.log('🖥️ Activating Nodes view');

        if (window.nodesView) {
            window.nodesView.showNodesView();
            console.log('✅ Nodes view activated');
        } else {
            console.error('❌ NodesView not available! Check initialization.');
            // Fallback to dashboard
            showDashboardView();
        }
    }

        // Add window focus/blur handlers
        window.addEventListener('focus', () => {
            if (!isPaused) {
                console.log('Window focused - resuming updates');
                forceRefresh();
            }
        });

        window.addEventListener('blur', () => {
            console.log('Window blurred');
            // You could pause updates when window is not visible to save resources
        });

        // Add periodic connection check
        window.connectionCheckInterval = setInterval(checkAPIConnection, 30000);
    // Check every 30 seconds
    }

// Export functions for global access
    window.togglePause = togglePause;
    window.forceRefresh = forceRefresh;
    window.isPaused = () => isPaused;

    console.log("🎯 Main.js loaded successfully");

function shutdownDashboard() {
    console.log("🛑 Shutting down dashboard...");

    // Stop KPI updates
    pauseUpdates();

    // Clear the 30s connection check interval
    if (window.connectionCheckInterval) {
        clearInterval(window.connectionCheckInterval);
        window.connectionCheckInterval = null;
    }

    // Optionally clean up nodesView
    if (window.nodesView) {
        window.nodesView.destroy?.(); // if you have a destroy method
        window.nodesView = null;
    }

    // Remove navigation event listeners if needed (optional)

    console.log("🧹 Dashboard shutdown complete.");
}

window.shutdownDashboard = shutdownDashboard;
