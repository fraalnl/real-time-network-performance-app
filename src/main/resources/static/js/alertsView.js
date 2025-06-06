// alertsView.js
// Handles the Alerts view with detailed alert management

const API_BASE_URL = 'http://localhost:8081/api/metrics';

class AlertsView {
    constructor() {
        this.activeAlerts = [];
        this.handledAlerts = [];
        this.updateInterval = null;
        this.isActive = false;
        this.currentTab = 'active'; // 'active' or 'handled'
    }

    initialize() {
        this.createAlertsView();
        console.log("🚨 Alerts View initialized");
    }

    createAlertsView() {
        // Create alerts view container
        const alertsViewHTML = `
            <div id="alertsView" class="view-container" style="display: none;">
                <div class="alerts-header">
                    <h2>Network Alerts Management</h2>
                    <div class="alerts-controls">
                        <button id="refreshAlerts" class="control-btn">🔄 Refresh</button>
                        <button id="clearHandledAlerts" class="control-btn danger">🗑️ Clear Handled</button>
                    </div>
                </div>

                <div class="alerts-stats">
                    <div class="stat-card">
                        <span class="stat-label">Active Alerts</span>
                        <span id="activeAlertsCount" class="stat-value stat-critical">--</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Handled Today</span>
                        <span id="handledAlertsCount" class="stat-value stat-good">--</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Critical Issues</span>
                        <span id="criticalAlertsCount" class="stat-value stat-critical">--</span>
                    </div>
                </div>

                <div class="alerts-tabs">
                    <button id="activeTab" class="tab-btn active" data-tab="active">
                        🔴 Active Alerts (<span id="activeTabCount">0</span>)
                    </button>
                    <button id="handledTab" class="tab-btn" data-tab="handled">
                        ✅ Handled Alerts (<span id="handledTabCount">0</span>)
                    </button>
                </div>

                <div class="alerts-content">
                    <!-- Active Alerts Section -->
                    <div id="activeAlertsSection" class="alerts-section">
                        <div class="alerts-list" id="activeAlertsList">
                            <!-- Active alerts will be populated here -->
                        </div>
                    </div>

                    <!-- Handled Alerts Section -->
                    <div id="handledAlertsSection" class="alerts-section" style="display: none;">
                        <div class="alerts-list" id="handledAlertsList">
                            <!-- Handled alerts will be populated here -->
                        </div>
                    </div>
                </div>

                <div id="alertsLoading" class="loading-indicator" style="display: none;">
                    <div class="spinner"></div>
                    <span>Loading alerts...</span>
                </div>

                <!-- Alert Detail Modal -->
                <div id="alertDetailModal" class="modal" style="display: none;">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3 id="modalAlertTitle">Alert Details</h3>
                            <button class="modal-close" id="closeModal">&times;</button>
                        </div>
                        <div class="modal-body" id="modalAlertBody">
                            <!-- Alert details will be populated here -->
                        </div>
                        <div class="modal-footer">
                            <button id="markResolvedBtn" class="btn btn-success">✅ Mark as Resolved</button>
                            <button id="cancelModalBtn" class="btn btn-secondary">Cancel</button>
                        </div>
                    </div>
                </div>

                <!-- Confirmation Modal -->
                <div id="confirmationModal" class="modal" style="display: none;">
                    <div class="modal-content confirmation-modal">
                        <div class="modal-header">
                            <h3 id="confirmationTitle">Confirm Action</h3>
                            <button class="modal-close" id="closeConfirmationModal">&times;</button>
                        </div>
                        <div class="modal-body" id="confirmationBody">
                            <div class="confirmation-icon">⚠️</div>
                            <p id="confirmationMessage">Are you sure you want to proceed?</p>
                        </div>
                        <div class="modal-footer">
                            <button id="confirmActionBtn" class="btn btn-danger">Confirm</button>
                            <button id="cancelActionBtn" class="btn btn-secondary">Cancel</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Insert into main content area
        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertAdjacentHTML('beforeend', alertsViewHTML);
        }

        this.setupEventListeners();
    }

    setupEventListeners() {
        // Refresh button
        const refreshBtn = document.getElementById('refreshAlerts');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadAlertsData();
            });
        }

        // Clear handled alerts button
        const clearBtn = document.getElementById('clearHandledAlerts');
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                this.clearHandledAlerts();
            });
        }

        // Tab switching
        const tabBtns = document.querySelectorAll('.tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const tabType = e.target.dataset.tab;
                this.switchTab(tabType);
            });
        });

        // Modal events
        const closeModal = document.getElementById('closeModal');
        const cancelModal = document.getElementById('cancelModalBtn');
        const markResolved = document.getElementById('markResolvedBtn');

        if (closeModal) closeModal.addEventListener('click', () => this.closeModal());
        if (cancelModal) cancelModal.addEventListener('click', () => this.closeModal());
        if (markResolved) markResolved.addEventListener('click', () => this.markAlertResolved());

        // Confirmation modal events
        const closeConfirmation = document.getElementById('closeConfirmationModal');
        const cancelAction = document.getElementById('cancelActionBtn');
        const confirmAction = document.getElementById('confirmActionBtn');

        if (closeConfirmation) closeConfirmation.addEventListener('click', () => this.closeConfirmationModal());
        if (cancelAction) cancelAction.addEventListener('click', () => this.closeConfirmationModal());
        if (confirmAction) confirmAction.addEventListener('click', () => this.executeConfirmedAction());

        // Close modals when clicking outside
        const alertModal = document.getElementById('alertDetailModal');
        const confirmModal = document.getElementById('confirmationModal');

        if (alertModal) {
            alertModal.addEventListener('click', (e) => {
                if (e.target === alertModal) this.closeModal();
            });
        }

        if (confirmModal) {
            confirmModal.addEventListener('click', (e) => {
                if (e.target === confirmModal) this.closeConfirmationModal();
            });
        }
    }

    async showAlertsView() {
        // Hide other views
        this.hideAllViews();

        // Show alerts view
        const alertsView = document.getElementById('alertsView');
        if (alertsView) {
            alertsView.style.display = 'block';
            this.isActive = true;

            // Load initial data
            await this.loadAlertsData();

            // Start auto-refresh every 10 seconds
            this.startAutoRefresh();

            console.log("🚨 Alerts view activated");
        }
    }

    hideAlertsView() {
        const alertsView = document.getElementById('alertsView');
        if (alertsView) {
            alertsView.style.display = 'none';
            this.isActive = false;
            this.stopAutoRefresh();
            this.closeModal();
            console.log("🚨 Alerts view deactivated");
        }
    }

    hideAllViews() {
        // Hide dashboard elements
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');
        const nodesView = document.getElementById('nodesView');

        if (kpiCards) kpiCards.style.display = 'none';
        if (chartContainer) chartContainer.style.display = 'none';
        if (nodesView) nodesView.style.display = 'none';
    }

    async loadAlertsData() {
        const loadingIndicator = document.getElementById('alertsLoading');

        try {
            if (loadingIndicator) loadingIndicator.style.display = 'flex';

            const token = localStorage.getItem("authToken");
            const headers = {};

            if (token) {
                headers['Authorization'] = 'Bearer ' + token;
            }

            // Fetch active alerts from API
            const response = await fetch(`${API_BASE_URL}/alerts`, {
                headers: headers
            });

            if (!response.ok) throw new Error('Failed to fetch alerts data');

            this.activeAlerts = await response.json();

            // Load handled alerts from localStorage
            this.loadHandledAlertsFromStorage();

            // Render both sections
            this.renderActiveAlerts();
            this.renderHandledAlerts();
            this.updateAlertsStats();

            console.log(`🚨 Loaded ${this.activeAlerts.length} active alerts and ${this.handledAlerts.length} handled alerts`);

        } catch (error) {
            console.error("❌ Error loading alerts data:", error);
            this.showError("Failed to load alerts data. Please try again.");
        } finally {
            if (loadingIndicator) loadingIndicator.style.display = 'none';
        }
    }

    loadHandledAlertsFromStorage() {
        const stored = localStorage.getItem('handledAlerts');
        if (stored) {
            try {
                this.handledAlerts = JSON.parse(stored);
                // Filter out old handled alerts (older than 24 hours)
                const oneDayAgo = new Date().getTime() - (24 * 60 * 60 * 1000);
                this.handledAlerts = this.handledAlerts.filter(alert =>
                    new Date(alert.handledAt).getTime() > oneDayAgo
                );
                this.saveHandledAlertsToStorage();
            } catch (e) {
                console.error("Error loading handled alerts from storage:", e);
                this.handledAlerts = [];
            }
        }
    }

    saveHandledAlertsToStorage() {
        try {
            localStorage.setItem('handledAlerts', JSON.stringify(this.handledAlerts));
        } catch (e) {
            console.error("Error saving handled alerts to storage:", e);
        }
    }

    renderActiveAlerts() {
        const container = document.getElementById('activeAlertsList');
        if (!container) return;

        if (this.activeAlerts.length === 0) {
            container.innerHTML = `
                <div class="no-alerts">
                    <div class="no-alerts-icon">🎉</div>
                    <h3>No Active Alerts</h3>
                    <p>All systems are running smoothly!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.activeAlerts.map(alert => this.createAlertCard(alert, 'active')).join('');
    }

    renderHandledAlerts() {
        const container = document.getElementById('handledAlertsList');
        if (!container) return;

        if (this.handledAlerts.length === 0) {
            container.innerHTML = `
                <div class="no-alerts">
                    <div class="no-alerts-icon">📋</div>
                    <h3>No Handled Alerts</h3>
                    <p>No alerts have been resolved today.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.handledAlerts.map(alert => this.createAlertCard(alert, 'handled')).join('');
    }

    createAlertCard(alert, type) {
        const severity = this.getAlertSeverity(alert);
        const timestamp = new Date(alert.timestamp).toLocaleString();
        const handledTime = type === 'handled' ? new Date(alert.handledAt).toLocaleString() : null;

        return `
            <div class="alert-card ${severity}" data-alert-id="${alert.nodeId}-${alert.timestamp}">
                <div class="alert-card-header">
                    <div class="alert-severity">
                        <span class="severity-icon">${this.getSeverityIcon(severity)}</span>
                        <span class="severity-text">${severity.toUpperCase()}</span>
                    </div>
                    <div class="alert-time">${timestamp}</div>
                </div>
                
                <div class="alert-card-body">
                    <h4>Node ${alert.nodeId} - Network ${alert.networkId}</h4>
                    <div class="alert-metrics">
                        <div class="metric">
                            <span class="metric-label">Latency:</span>
                            <span class="metric-value ${alert.latency > 100 ? 'critical' : ''}">${alert.latency}ms</span>
                        </div>
                        <div class="metric">
                            <span class="metric-label">Throughput:</span>
                            <span class="metric-value ${alert.throughput < 50 ? 'critical' : ''}">${alert.throughput} Mbps</span>
                        </div>
                        <div class="metric">
                            <span class="metric-label">Error Rate:</span>
                            <span class="metric-value ${alert.errorRate > 2 ? 'critical' : ''}">${alert.errorRate}%</span>
                        </div>
                    </div>
                    
                    ${type === 'handled' ? `
                        <div class="handled-info">
                            <span class="handled-label">✅ Resolved at: ${handledTime}</span>
                        </div>
                    ` : ''}
                </div>
                
                <div class="alert-card-actions">
                    <button class="btn btn-info" onclick="window.alertsView.showAlertDetails('${alert.nodeId}', '${alert.timestamp}', '${type}')">
                        📋 View Details
                    </button>
                    ${type === 'active' ? `
                        <button class="btn btn-success" onclick="window.alertsView.quickResolve('${alert.nodeId}', '${alert.timestamp}')">
                            ✅ Mark Resolved
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
    }

    getAlertSeverity(alert) {
        if (alert.latency > 200 || alert.throughput < 25 || alert.errorRate > 4) {
            return 'critical';
        } else if (alert.latency > 100 || alert.throughput < 50 || alert.errorRate > 2) {
            return 'warning';
        }
        return 'info';
    }

    getSeverityIcon(severity) {
        switch (severity) {
            case 'critical': return '🔴';
            case 'warning': return '🟡';
            case 'info': return '🔵';
            default: return '⚠️';
        }
    }

    switchTab(tabType) {
        this.currentTab = tabType;

        // Update tab buttons
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.getElementById(`${tabType}Tab`).classList.add('active');

        // Show/hide sections
        if (tabType === 'active') {
            document.getElementById('activeAlertsSection').style.display = 'block';
            document.getElementById('handledAlertsSection').style.display = 'none';
        } else {
            document.getElementById('activeAlertsSection').style.display = 'none';
            document.getElementById('handledAlertsSection').style.display = 'block';
        }
    }

    showAlertDetails(nodeId, timestamp, type) {
        const alerts = type === 'active' ? this.activeAlerts : this.handledAlerts;
        const alert = alerts.find(a => a.nodeId == nodeId && a.timestamp === timestamp);

        if (!alert) return;

        const modal = document.getElementById('alertDetailModal');
        const title = document.getElementById('modalAlertTitle');
        const body = document.getElementById('modalAlertBody');
        const resolveBtn = document.getElementById('markResolvedBtn');

        title.textContent = `Alert Details - Node ${alert.nodeId}`;

        const severity = this.getAlertSeverity(alert);
        body.innerHTML = `
            <div class="alert-detail">
                <div class="detail-section">
                    <h4>📍 Location Information</h4>
                    <p><strong>Node ID:</strong> ${alert.nodeId}</p>
                    <p><strong>Network ID:</strong> ${alert.networkId}</p>
                    <p><strong>Alert Time:</strong> ${new Date(alert.timestamp).toLocaleString()}</p>
                    ${type === 'handled' ? `<p><strong>Resolved Time:</strong> ${new Date(alert.handledAt).toLocaleString()}</p>` : ''}
                </div>
                
                <div class="detail-section">
                    <h4>🔥 Severity Level</h4>
                    <div class="severity-display ${severity}">
                        ${this.getSeverityIcon(severity)} ${severity.toUpperCase()}
                    </div>
                </div>
                
                <div class="detail-section">
                    <h4>📊 Performance Metrics</h4>
                    <div class="metrics-grid">
                        <div class="metric-box ${alert.latency > 100 ? 'critical' : alert.latency > 50 ? 'warning' : 'good'}">
                            <label>Latency</label>
                            <value>${alert.latency} ms</value>
                            <status>${alert.latency > 100 ? 'CRITICAL' : alert.latency > 50 ? 'WARNING' : 'NORMAL'}</status>
                        </div>
                        <div class="metric-box ${alert.throughput < 50 ? 'critical' : alert.throughput < 80 ? 'warning' : 'good'}">
                            <label>Throughput</label>
                            <value>${alert.throughput} Mbps</value>
                            <status>${alert.throughput < 50 ? 'CRITICAL' : alert.throughput < 80 ? 'WARNING' : 'NORMAL'}</status>
                        </div>
                        <div class="metric-box ${alert.errorRate > 2 ? 'critical' : alert.errorRate > 1 ? 'warning' : 'good'}">
                            <label>Error Rate</label>
                            <value>${alert.errorRate}%</value>
                            <status>${alert.errorRate > 2 ? 'CRITICAL' : alert.errorRate > 1 ? 'WARNING' : 'NORMAL'}</status>
                        </div>
                    </div>
                </div>
                
                <div class="detail-section">
                    <h4>💡 Recommended Actions</h4>
                    <ul class="recommendations">
                        ${this.getRecommendations(alert).map(rec => `<li>${rec}</li>`).join('')}
                    </ul>
                </div>
            </div>
        `;

        // Store current alert for resolution
        this.currentAlertForResolution = { nodeId, timestamp, type };

        // Show/hide resolve button
        if (type === 'active') {
            resolveBtn.style.display = 'inline-block';
        } else {
            resolveBtn.style.display = 'none';
        }

        modal.style.display = 'flex';
    }

    getRecommendations(alert) {
        const recommendations = [];

        if (alert.latency > 100) {
            recommendations.push("🔧 Check network routing and congestion");
            recommendations.push("📡 Verify signal strength and interference");
        }

        if (alert.throughput < 50) {
            recommendations.push("📊 Analyze bandwidth utilization");
            recommendations.push("⚡ Check for hardware bottlenecks");
        }

        if (alert.errorRate > 2) {
            recommendations.push("🔍 Investigate error logs");
            recommendations.push("🛠️ Check hardware health status");
        }

        if (recommendations.length === 0) {
            recommendations.push("✅ Monitor for trend changes");
            recommendations.push("📈 Continue regular maintenance");
        }

        return recommendations;
    }

    quickResolve(nodeId, timestamp) {
        this.resolveAlert(nodeId, timestamp);
    }

    markAlertResolved() {
        if (this.currentAlertForResolution) {
            this.resolveAlert(
                this.currentAlertForResolution.nodeId,
                this.currentAlertForResolution.timestamp
            );
            this.closeModal();
        }
    }

    resolveAlert(nodeId, timestamp) {
        const alertIndex = this.activeAlerts.findIndex(a => a.nodeId == nodeId && a.timestamp === timestamp);

        if (alertIndex !== -1) {
            const resolvedAlert = { ...this.activeAlerts[alertIndex] };
            resolvedAlert.handledAt = new Date().toISOString();

            // Move to handled alerts
            this.handledAlerts.unshift(resolvedAlert);
            this.activeAlerts.splice(alertIndex, 1);

            // Save to storage
            this.saveHandledAlertsToStorage();

            // Re-render
            this.renderActiveAlerts();
            this.renderHandledAlerts();
            this.updateAlertsStats();

            console.log(`✅ Alert resolved for Node ${nodeId}`);
        }
    }

    closeModal() {
        const modal = document.getElementById('alertDetailModal');
        if (modal) {
            modal.style.display = 'none';
        }
        this.currentAlertForResolution = null;
    }

    clearHandledAlerts() {
        this.showConfirmationModal(
            'Clear Handled Alerts',
            'Are you sure you want to clear all handled alerts? This action cannot be undone.',
            '🗑️',
            () => {
                this.handledAlerts = [];
                this.saveHandledAlertsToStorage();
                this.renderHandledAlerts();
                this.updateAlertsStats();
                console.log("🗑️ Handled alerts cleared");
                this.showSuccessMessage("All handled alerts have been cleared successfully.");
            }
        );
    }

    showConfirmationModal(title, message, icon, confirmCallback) {
        const modal = document.getElementById('confirmationModal');
        const titleElement = document.getElementById('confirmationTitle');
        const messageElement = document.getElementById('confirmationMessage');
        const iconElement = document.querySelector('.confirmation-icon');

        if (titleElement) titleElement.textContent = title;
        if (messageElement) messageElement.textContent = message;
        if (iconElement) iconElement.textContent = icon;

        // Store the callback for execution
        this.pendingConfirmAction = confirmCallback;

        if (modal) modal.style.display = 'flex';
    }

    executeConfirmedAction() {
        if (this.pendingConfirmAction) {
            this.pendingConfirmAction();
            this.pendingConfirmAction = null;
        }
        this.closeConfirmationModal();
    }

    closeConfirmationModal() {
        const modal = document.getElementById('confirmationModal');
        if (modal) {
            modal.style.display = 'none';
        }
        this.pendingConfirmAction = null;
    }

    showSuccessMessage(message) {
        // Create a temporary success notification
        const notification = document.createElement('div');
        notification.className = 'success-notification';
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">✅</span>
                <span class="notification-message">${message}</span>
            </div>
        `;

        document.body.appendChild(notification);

        // Auto-remove after 3 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 3000);
    }

    updateAlertsStats() {
        const activeCount = this.activeAlerts.length;
        const handledCount = this.handledAlerts.length;
        const criticalCount = this.activeAlerts.filter(a => this.getAlertSeverity(a) === 'critical').length;

        // Update stats
        document.getElementById('activeAlertsCount').textContent = activeCount;
        document.getElementById('handledAlertsCount').textContent = handledCount;
        document.getElementById('criticalAlertsCount').textContent = criticalCount;

        // Update tab counts
        document.getElementById('activeTabCount').textContent = activeCount;
        document.getElementById('handledTabCount').textContent = handledCount;
    }

    startAutoRefresh() {
        this.stopAutoRefresh();
        this.updateInterval = setInterval(() => {
            if (this.isActive) {
                this.loadAlertsData();
            }
        }, 10000); // Refresh every 10 seconds
    }

    stopAutoRefresh() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
            this.updateInterval = null;
        }
    }

    showError(message) {
        const activeList = document.getElementById('activeAlertsList');
        const handledList = document.getElementById('handledAlertsList');

        const errorHTML = `
            <div class="error-message">
                ❌ ${message}
            </div>
        `;

        if (activeList) activeList.innerHTML = errorHTML;
        if (handledList) handledList.innerHTML = errorHTML;
    }

    // Public methods
    isViewActive() {
        return this.isActive;
    }

    getActiveAlertsCount() {
        return this.activeAlerts.length;
    }

    getHandledAlertsCount() {
        return this.handledAlerts.length;
    }
}

// Export for use in other modules
export { AlertsView };

// Global instance
window.alertsView = new AlertsView();

console.log("🚨 AlertsView module loaded");