// nodesView.js
// Handles the Nodes view with data table functionality

const API_BASE_URL = 'http://localhost:8081/api/metrics';

class NodesView {
    constructor() {
        this.nodesData = [];
        this.sortColumn = 'nodeId';
        this.sortDirection = 'asc';
        this.filterText = '';
        this.updateInterval = null;
        this.isActive = false;
    }

    initialize() {
        this.createNodesView();
        console.log("📋 Nodes View initialized");
    }

    createNodesView() {
        // Create nodes view container
        const nodesViewHTML = `
            <div id="nodesView" class="view-container" style="display: none;">
                <div class="nodes-header">
                    <h2>Network Nodes Overview</h2>
                    <div class="nodes-controls">
                        <input type="text" id="nodeSearch" placeholder="Search nodes..." class="search-input">
                        <select id="networkFilter" class="filter-select">
                            <option value="">All Networks</option>
                            <option value="201">Network 201</option>
                            <option value="202">Network 202</option>
                            <option value="203">Network 203</option>
                            <option value="204">Network 204</option>
                        </select>
                        <button id="refreshNodes" class="control-btn">🔄 Refresh</button>
                    </div>
                </div>

                <div class="nodes-stats">
                    <div class="stat-card">
                        <span class="stat-label">Total Nodes</span>
                        <span id="totalNodesCount" class="stat-value">--</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Healthy</span>
                        <span id="healthyNodesCount" class="stat-value stat-good">--</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Warning</span>
                        <span id="warningNodesCount" class="stat-value stat-warning">--</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Critical</span>
                        <span id="criticalNodesCount" class="stat-value stat-critical">--</span>
                    </div>
                </div>

                <div class="nodes-table-container">
                    <table id="nodesTable" class="nodes-table">
                        <thead>
                            <tr>
                                <th data-column="nodeId" class="sortable">
                                    Node ID <span class="sort-arrow">↕️</span>
                                </th>
                                <th data-column="networkId" class="sortable">
                                    Network <span class="sort-arrow">↕️</span>
                                </th>
                                <th data-column="latency" class="sortable">
                                    Latency (ms) <span class="sort-arrow">↕️</span>
                                </th>
                                <th data-column="throughput" class="sortable">
                                    Throughput (Mbps) <span class="sort-arrow">↕️</span>
                                </th>
                                <th data-column="errorRate" class="sortable">
                                    Error Rate (%) <span class="sort-arrow">↕️</span>
                                </th>
                                <th data-column="timestamp" class="sortable">
                                    Last Updated <span class="sort-arrow">↕️</span>
                                </th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody id="nodesTableBody">
                            <!-- Nodes data will be populated here -->
                        </tbody>
                    </table>
                </div>

                <div id="nodesLoading" class="loading-indicator" style="display: none;">
                    <div class="spinner"></div>
                    <span>Loading nodes data...</span>
                </div>
            </div>
        `;

        // Insert into main content area
        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertAdjacentHTML('beforeend', nodesViewHTML);
        }

        this.setupEventListeners();
    }

    setupEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('nodeSearch');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterText = e.target.value.toLowerCase();
                this.renderNodesTable();
            });
        }

        // Network filter
        const networkFilter = document.getElementById('networkFilter');
        if (networkFilter) {
            networkFilter.addEventListener('change', () => {
                this.renderNodesTable();
            });
        }

        // Refresh button
        const refreshBtn = document.getElementById('refreshNodes');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadNodesData();
            });
        }

        // Table sorting
        const sortableHeaders = document.querySelectorAll('.sortable');
        sortableHeaders.forEach(header => {
            header.addEventListener('click', (e) => {
                const column = e.target.closest('th').dataset.column;
                this.sortTable(column);
            });
        });
    }

    async showNodesView() {
        // Hide other views
        this.hideAllViews();

        // Show nodes view
        const nodesView = document.getElementById('nodesView');
        if (nodesView) {
            nodesView.style.display = 'block';
            this.isActive = true;

            // Load initial data
            await this.loadNodesData();

            // Start auto-refresh every 5 seconds
            this.startAutoRefresh();

            console.log("📋 Nodes view activated");
        }
    }

    hideNodesView() {
        const nodesView = document.getElementById('nodesView');
        if (nodesView) {
            nodesView.style.display = 'none';
            this.isActive = false;
            this.stopAutoRefresh();
            console.log("📋 Nodes view deactivated");
        }
    }

    hideAllViews() {
        // Hide dashboard elements
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');

        if (kpiCards) kpiCards.style.display = 'none';
        if (chartContainer) chartContainer.style.display = 'none';
    }

    showDashboardView() {
        // Show dashboard elements
        const kpiCards = document.querySelector('.kpi-cards');
        const chartContainer = document.querySelector('.chart-container');

        if (kpiCards) kpiCards.style.display = 'grid';
        if (chartContainer) chartContainer.style.display = 'block';

        this.hideNodesView();
    }

    async loadNodesData() {
        const loadingIndicator = document.getElementById('nodesLoading');

        try {
            if (loadingIndicator) loadingIndicator.style.display = 'flex';

            const response = await fetch(`${API_BASE_URL}/realtime`);
            if (!response.ok) throw new Error('Failed to fetch nodes data');

            this.nodesData = await response.json();
            this.renderNodesTable();
            this.updateNodesStats();

            console.log(`📋 Loaded data for ${this.nodesData.length} nodes`);

        } catch (error) {
            console.error("❌ Error loading nodes data:", error);
            this.showError("Failed to load nodes data. Please try again.");
        } finally {
            if (loadingIndicator) loadingIndicator.style.display = 'none';
        }
    }

    renderNodesTable() {
        const tbody = document.getElementById('nodesTableBody');
        if (!tbody) return;

        // Apply filters
        let filteredData = this.nodesData.filter(node => {
            const matchesSearch = this.filterText === '' ||
                node.nodeId.toString().includes(this.filterText) ||
                node.networkId.toString().includes(this.filterText);

            const networkFilter = document.getElementById('networkFilter')?.value;
            const matchesNetwork = !networkFilter || node.networkId.toString() === networkFilter;

            return matchesSearch && matchesNetwork;
        });

        // Apply sorting
        filteredData.sort((a, b) => {
            let aVal = a[this.sortColumn];
            let bVal = b[this.sortColumn];

            // Handle different data types
            if (this.sortColumn === 'timestamp') {
                aVal = new Date(aVal);
                bVal = new Date(bVal);
            } else if (typeof aVal === 'string') {
                aVal = aVal.toLowerCase();
                bVal = bVal.toLowerCase();
            }

            if (aVal < bVal) return this.sortDirection === 'asc' ? -1 : 1;
            if (aVal > bVal) return this.sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        // Generate table rows
        tbody.innerHTML = filteredData.map(node => {
            const status = this.getNodeStatus(node);
            const timestamp = new Date(node.timestamp).toLocaleString();

            return `
                <tr class="node-row ${status.class}">
                    <td class="node-id">${node.nodeId}</td>
                    <td class="network-id">Network ${node.networkId}</td>
                    <td class="latency ${this.getLatencyClass(node.latency)}">${node.latency} ms</td>
                    <td class="throughput ${this.getThroughputClass(node.throughput)}">${node.throughput} Mbps</td>
                    <td class="error-rate ${this.getErrorRateClass(node.errorRate)}">${node.errorRate}%</td>
                    <td class="timestamp">${timestamp}</td>
                    <td class="status">
                        <span class="status-badge ${status.class}">${status.icon} ${status.text}</span>
                    </td>
                </tr>
            `;
        }).join('');

        // Update sort arrows
        this.updateSortArrows();
    }

    sortTable(column) {
        if (this.sortColumn === column) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortColumn = column;
            this.sortDirection = 'asc';
        }

        this.renderNodesTable();
    }

    updateSortArrows() {
        // Reset all arrows
        document.querySelectorAll('.sort-arrow').forEach(arrow => {
            arrow.textContent = '↕️';
        });

        // Set active arrow
        const activeHeader = document.querySelector(`[data-column="${this.sortColumn}"] .sort-arrow`);
        if (activeHeader) {
            activeHeader.textContent = this.sortDirection === 'asc' ? '↑' : '↓';
        }
    }

    updateNodesStats() {
        const totalCount = this.nodesData.length;
        let healthyCount = 0, warningCount = 0, criticalCount = 0;

        this.nodesData.forEach(node => {
            const status = this.getNodeStatus(node);
            switch (status.class) {
                case 'healthy': healthyCount++; break;
                case 'warning': warningCount++; break;
                case 'critical': criticalCount++; break;
            }
        });

        document.getElementById('totalNodesCount').textContent = totalCount;
        document.getElementById('healthyNodesCount').textContent = healthyCount;
        document.getElementById('warningNodesCount').textContent = warningCount;
        document.getElementById('criticalNodesCount').textContent = criticalCount;
    }

    getNodeStatus(node) {
        if (node.latency > 100 || node.throughput < 50 || node.errorRate > 2) {
            return { class: 'critical', icon: '🔴', text: 'Critical' };
        } else if (node.latency > 50 || node.throughput < 80 || node.errorRate > 1) {
            return { class: 'warning', icon: '🟡', text: 'Warning' };
        } else {
            return { class: 'healthy', icon: '🟢', text: 'Healthy' };
        }
    }

    getLatencyClass(latency) {
        if (latency > 100) return 'metric-critical';
        if (latency > 50) return 'metric-warning';
        return 'metric-good';
    }

    getThroughputClass(throughput) {
        if (throughput < 50) return 'metric-critical';
        if (throughput < 80) return 'metric-warning';
        return 'metric-good';
    }

    getErrorRateClass(errorRate) {
        if (errorRate > 2) return 'metric-critical';
        if (errorRate > 1) return 'metric-warning';
        return 'metric-good';
    }

    startAutoRefresh() {
        this.stopAutoRefresh(); // Clear any existing interval
        this.updateInterval = setInterval(() => {
            if (this.isActive) {
                this.loadNodesData();
            }
        }, 5000); // Refresh every 5 seconds
    }

    stopAutoRefresh() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
            this.updateInterval = null;
        }
    }

    showError(message) {
        const tbody = document.getElementById('nodesTableBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="error-message">
                        ❌ ${message}
                    </td>
                </tr>
            `;
        }
    }

    // Public methods
    isViewActive() {
        return this.isActive;
    }

    getNodesData() {
        return this.nodesData;
    }
}

// Export for use in other modules
export { NodesView };

// Global instance
window.nodesView = new NodesView();

console.log("📋 NodesView module loaded");