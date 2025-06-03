// alertsManager.js
// Handles anomaly detection and alert notifications

const API_BASE_URL = 'http://localhost:8081/api/metrics';

class AlertsManager {
    constructor() {
        this.alerts = [];
        this.lastAlertCheck = null;
        this.alertContainer = null;
        this.soundEnabled = false; // You can make this configurable
    }

    async initialize() {
        this.createAlertContainer();
        await this.startAlertMonitoring();
        console.log("🚨 Alerts Manager initialized");
    }

    createAlertContainer() {
        // Create alerts container if it doesn't exist
        this.alertContainer = document.getElementById('alertsContainer');

        if (!this.alertContainer) {
            this.alertContainer = document.createElement('div');
            this.alertContainer.id = 'alertsContainer';
            this.alertContainer.className = 'alerts-container';
            document.body.appendChild(this.alertContainer);
        }
    }

    async startAlertMonitoring() {
        // Check for alerts every 10 seconds
        setInterval(() => {
            this.checkForAlerts();
        }, 10000);

        // Initial check
        await this.checkForAlerts();
    }

    async checkForAlerts() {
        try {
            const token = localStorage.getItem("authToken");
            const headers = {};

            if (token) {
                headers['Authorization'] = 'Bearer ' + token;
            }

            const response = await fetch(`${API_BASE_URL}/alerts`, {
                headers: headers
            });

            if (!response.ok) throw new Error('Failed to fetch alerts');

            const alerts = await response.json();
            this.processAlerts(alerts);

        } catch (error) {
            console.error("❌ Error checking alerts:", error);
        }
    }

    processAlerts(newAlerts) {
        // Filter out alerts we've already seen recently
        const recentAlerts = newAlerts.filter(alert => {
            const alertTime = new Date(alert.timestamp);
            return !this.lastAlertCheck || alertTime > this.lastAlertCheck;
        });

        if (recentAlerts.length > 0) {
            console.log(`🚨 ${recentAlerts.length} new alerts detected`);

            recentAlerts.forEach(alert => {
                this.showAlert(alert);
            });

            // Update sidebar alert count
            this.updateAlertBadge(newAlerts.length);
        }

        this.alerts = newAlerts;
        this.lastAlertCheck = new Date();
    }

    showAlert(alertData) {
        const alertElement = this.createAlertElement(alertData);
        this.alertContainer.appendChild(alertElement);

        // Auto-remove after 10 seconds
        setTimeout(() => {
            if (alertElement && alertElement.parentNode) {
                alertElement.remove();
            }
        }, 10000);

        // Play alert sound if enabled
        if (this.soundEnabled) {
            this.playAlertSound();
        }
    }

    createAlertElement(alertData) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${this.getAlertSeverity(alertData)}`;

        const timestamp = new Date(alertData.timestamp).toLocaleTimeString();
        const severity = this.getAlertSeverity(alertData);
        const icon = this.getAlertIcon(severity);
        const { nodeId, latency, throughput, errorRate, networkId } = alertData;

        alertDiv.innerHTML = `
            <div class="alert-header">
                <span class="alert-icon">${icon}</span>
                <span class="alert-title">Node ${nodeId} Alert</span>
                <button class="alert-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
            <div class="alert-body">
                <div class="alert-metrics">
                    <span>Latency: ${latency}ms</span>
                    <span>Throughput: ${throughput}Mbps</span>
                    <span>Error Rate: ${errorRate}%</span>
                </div>
                <div class="alert-time">Network ${networkId} • ${timestamp}</div>
            </div>
        `;

        return alertDiv;
    }

    getAlertSeverity(alertData) {
        if (alertData.latency > 200 || alertData.throughput < 25 || alertData.errorRate > 4) {
            return 'critical';
        } else if (alertData.latency > 100 || alertData.throughput < 50 || alertData.errorRate > 2) {
            return 'warning';
        }
        return 'info';
    }

    getAlertIcon(severity) {
        switch (severity) {
            case 'critical': return '🔴';
            case 'warning': return '🟡';
            case 'info': return '🔵';
            default: return '⚠️';
        }
    }

    updateAlertBadge(count) {
        const alertBadge = document.getElementById('alertBadge');
        if (alertBadge) {
            alertBadge.textContent = count;
            alertBadge.style.display = count > 0 ? 'inline' : 'none';
        }

        // Update sidebar item
        const alertsMenuItem = document.querySelector('.sidebar li:nth-child(4)'); // Alerts menu item
        if (alertsMenuItem && count > 0) {
            alertsMenuItem.innerHTML = `Alerts <span class="badge">${count}</span>`;
        }
    }

    playAlertSound() {
        try {
            // Simple beep sound (you can replace with actual sound file)
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            const audioContext = new AudioContext();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            oscillator.frequency.value = 800;
            oscillator.type = 'sine';
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);

            oscillator.start();
            oscillator.stop(audioContext.currentTime + 0.2);
        } catch (error) {
            console.warn("Audio playback failed:", error);
        }
    }
}

// Export for use in other modules
export { AlertsManager };

// Global instance
//window.alertsManager = new AlertsManager();