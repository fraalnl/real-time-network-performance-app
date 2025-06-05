// analyticsView.js
// Handles the Analytics view with historical KPI summary

const API_BASE_URL = 'http://localhost:8081/api/metrics';

class AnalyticsView {
    constructor() {
        this.analyticsView = null;
        this.isActive = false;
    }

    initialize() {
        this.createAnalyticsView();
        this.setupEventListeners();
        console.log("📊 Analytics View initialized");
    }

    createAnalyticsView() {
        const analyticsHTML = `
            <div id="analyticsView" class="view-container" style="display: none;">
                <div class="analytics-header">
                    <h2>Historical KPI Summary</h2>
                    <div class="analytics-controls">
                        <button class="control-btn" id="range5min">Last 5 Minutes</button>
                        <button class="control-btn" id="range1hr">Last 1 Hour</button>
                        <button class="control-btn" id="range24hr">Last 24 Hours</button>
                    </div>
                </div>

                <div class="analytics-summary bg-dark text-light border rounded p-3">
                    <p><strong>Latency:</strong> <span id="latencyVal">-</span> ms</p>
                    <p><strong>Throughput:</strong> <span id="throughputVal">-</span> Mbps</p>
                    <p><strong>Error Rate:</strong> <span id="errorRateVal">-</span> %</p>
                    <p><strong>Samples:</strong> <span id="sampleCount">-</span></p>
                </div>
            </div>
        `;

        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertAdjacentHTML('beforeend', analyticsHTML);
            this.analyticsView = document.getElementById('analyticsView');
        }
    }

    setupEventListeners() {
        document.getElementById('range5min')?.addEventListener('click', () => this.loadKpiSummary('last_5_minutes'));
        document.getElementById('range1hr')?.addEventListener('click', () => this.loadKpiSummary('last_1_hour'));
        document.getElementById('range24hr')?.addEventListener('click', () => this.loadKpiSummary('last_24_hours'));
    }

    async showAnalyticsView() {
        this.hideAllViews();

        if (this.analyticsView) {
            this.analyticsView.style.display = 'block';
            this.isActive = true;
            this.loadKpiSummary('last_5_minutes');
            console.log("📊 Analytics view activated");
        }
    }

    hideAnalyticsView() {
        if (this.analyticsView) {
            this.analyticsView.style.display = 'none';
            this.isActive = false;
            console.log("📊 Analytics view deactivated");
        }
    }

    hideAllViews() {
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');
        const nodesView = document.getElementById('nodesView');

        if (kpiCards) kpiCards.style.display = 'none';
        if (chartContainer) chartContainer.style.display = 'none';
        if (nodesView) nodesView.style.display = 'none';
    }

    async loadKpiSummary(range) {
        try {
            const token = localStorage.getItem("authToken");
            const headers = token ? { 'Authorization': 'Bearer ' + token } : {};

            const response = await fetch(`${API_BASE_URL}/summary/range?range=${range}`, {
                method: 'GET',
                headers
            });

            if (!response.ok) throw new Error("Failed to load KPI summary");

            const data = await response.json();

            document.getElementById('latencyVal').innerText = data.avgLatency.toFixed(2);
            document.getElementById('throughputVal').innerText = data.avgThroughput.toFixed(2);
            document.getElementById('errorRateVal').innerText = data.avgErrorRate.toFixed(2);
            document.getElementById('sampleCount').innerText = data.sampleSize;

            console.log(`📈 Loaded KPI summary for ${range}`);
        } catch (error) {
            console.error("❌ Error loading KPI summary:", error);
        }
    }

    isViewActive() {
        return this.isActive;
    }
}

export { AnalyticsView };

// Global instance
window.analyticsView = new AnalyticsView();

console.log("📊 AnalyticsView module loaded");
