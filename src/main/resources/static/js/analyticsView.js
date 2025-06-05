// analyticsView.js
// Redesigned to match alerts style with tab-based layout

const API_BASE_URL = 'http://localhost:8081/api/metrics';

class AnalyticsView {
    constructor() {
        this.isActive = false;
    }

    initialize() {
        this.createAnalyticsView();
        console.log("📊 Analytics View initialized");
    }

    createAnalyticsView() {
        const analyticsHTML = `
            <div id="analyticsView" class="view-container" style="display: none;">
                <div class="analytics-header">
                    <h2>Analytics</h2>
                    <p class="text-muted">Historical KPI Summary based on selected time ranges</p>
                </div>

                <div class="analytics-tabs">
                    <button id="tab5m" class="tab-btn active" data-tab="5m">⏱️ Last 5 Minutes</button>
                    <button id="tab1h" class="tab-btn" data-tab="1h">🕒 Last 1 Hour</button>
                    <button id="tab24h" class="tab-btn" data-tab="24h">🗓️ Last 24 Hours</button>
                </div>

                <div class="analytics-content">
                    <div id="analyticsSummary" class="analytics-section">
                        <!-- KPI summary will be populated here -->
                    </div>
                </div>

                <div id="analyticsLoading" class="loading-indicator" style="display: none;">
                    <div class="spinner"></div>
                    <span>Loading summary...</span>
                </div>
            </div>
        `;

        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertAdjacentHTML('beforeend', analyticsHTML);
        }

        this.setupEventListeners();
    }

    setupEventListeners() {
        const tabs = document.querySelectorAll('.analytics-tabs .tab-btn');
        tabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                e.preventDefault();

                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const range = tab.dataset.tab;
                this.loadAnalyticsSummary(range);
            });
        });
    }

    async showAnalyticsView() {
        this.hideAllViews();

        const analyticsView = document.getElementById('analyticsView');
        if (analyticsView) {
            analyticsView.style.display = 'block';
            this.isActive = true;
            console.log("📊 Analytics view displayed");

            const activeTab = document.querySelector('.analytics-tabs .tab-btn.active');
            if (activeTab) {
                const range = activeTab.dataset.tab;
                this.loadAnalyticsSummary(range);
            }
        }
    }

    hideAnalyticsView() {
        const analyticsView = document.getElementById('analyticsView');
        if (analyticsView) {
            analyticsView.style.display = 'none';
            this.isActive = false;
            console.log("📊 Analytics view hidden");
        }
    }

    hideAllViews() {
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');
        const nodesView = document.getElementById('nodesView');
        const alertsView = document.getElementById('alertsView');

        if (kpiCards) kpiCards.style.display = 'none';
        if (chartContainer) chartContainer.style.display = 'none';
        if (nodesView) nodesView.style.display = 'none';
        if (alertsView) alertsView.style.display = 'none';
    }

    async loadAnalyticsSummary(range) {
        const loading = document.getElementById('analyticsLoading');
        const container = document.getElementById('analyticsSummary');

        try {
            if (loading) loading.style.display = 'flex';
            if (container) container.innerHTML = '';

            const token = localStorage.getItem("authToken");
            const headers = token ? { 'Authorization': 'Bearer ' + token } : {};
            const response = await fetch(`${API_BASE_URL}/summary?range=${range}`, { headers });

            if (!response.ok) throw new Error('Failed to load KPI summary');

            const data = await response.json();
            if (container) {
                container.innerHTML = `
                    <div><strong>Latency:</strong> ${data.averageLatency} ms</div>
                    <div><strong>Throughput:</strong> ${data.averageThroughput} Mbps</div>
                    <div><strong>Error Rate:</strong> ${data.averageErrorRate} %</div>
                `;
            }
        } catch (err) {
            console.error("❌ Error fetching analytics summary:", err);
            if (container) container.innerHTML = '❌ Error loading summary';
        } finally {
            if (loading) loading.style.display = 'none';
        }
    }

    isViewActive() {
        return this.isActive;
    }
}

export { AnalyticsView };
window.analyticsView = new AnalyticsView();
console.log("📊 AnalyticsView module loaded");
