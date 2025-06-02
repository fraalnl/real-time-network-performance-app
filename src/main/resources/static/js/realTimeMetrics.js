// realTimeMetrics.js
let kpiChart;
let lastUpdateTime = null;
let updateIntervalId = null; // Store interval ID for pausing
let isPaused = false; // Track pause state

// Configuration
const API_BASE_URL = 'http://localhost:8081/api/metrics'; // Update with your Spring Boot URL
const UPDATE_INTERVAL = 3000; // 3 seconds

export function startRealTimeKPIUpdates() {
    console.log("🚀 Starting Real-Time KPI Updates");
    initializeChart();
    updateKPIData(); // Initial load
   // setInterval(updateKPIData, UPDATE_INTERVAL);
    startUpdateInterval(); // Start the interval

}

function startUpdateInterval() {
    if (updateIntervalId) {
        clearInterval(updateIntervalId);
    }

    updateIntervalId = setInterval(() => {
        if (!isPaused) {
            updateKPIData();
        }
    }, UPDATE_INTERVAL);
}

export function pauseUpdates() {
    isPaused = true;
    console.log("⏸️ KPI updates paused");
}

export function resumeUpdates() {
    isPaused = false;
    console.log("▶️ KPI updates resumed");
    updateKPIData(); // Immediate update when resuming
}

export function isPausedState() {
    return isPaused;
}

function initializeChart() {
    const ctx = document.getElementById('kpiChart').getContext('2d');
    kpiChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Avg Latency (ms)',
                    borderColor: '#ff6b6b',
                    backgroundColor: 'rgba(255, 107, 107, 0.1)',
                    data: [],
                    fill: false,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.2
                },
                {
                    label: 'Avg Throughput (Mbps)',
                    borderColor: '#4ecdc4',
                    backgroundColor: 'rgba(78, 205, 196, 0.1)',
                    data: [],
                    fill: false,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.2
                },
                {
                    label: 'Avg Error Rate (%)',
                    borderColor: '#ffe66d',
                    backgroundColor: 'rgba(255, 230, 109, 0.1)',
                    data: [],
                    fill: false,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    backgroundColor: '#1a1a1a',
                    titleColor: '#fff',
                    bodyColor: '#ccc',
                    borderColor: '#444',
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: function (context) {
                            let value = context.formattedValue;
                            return ` ${context.dataset.label}: ${value}`;
                        }
                    }
                },
                legend: {
                    labels: {
                        color: '#ccc',
                        boxWidth: 12,
                        padding: 20
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: '#aaa',
                        maxTicksLimit: 10
                    },
                    grid: { color: '#333' }
                },
                y: {
                    ticks: { color: '#aaa' },
                    grid: { color: '#333' }
                }
            },
            animation: {
                duration: 750
            }
        }
    });
}

async function updateKPIData() {
    try {
        // Fetch real-time metrics from your REST API
        const [summaryResponse, realtimeResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/summary`),
            fetch(`${API_BASE_URL}/realtime`)
        ]);

        if (!summaryResponse.ok || !realtimeResponse.ok) {
            throw new Error('API request failed');
        }

        const summary = await summaryResponse.json();
        const realtimeData = await realtimeResponse.json();

        // Update KPI cards with real data
        updateKPICards(summary);

        // Update chart with aggregated metrics
        updateChart(summary);

        // Update status indicators
        updateSystemStatus(summary, realtimeData);

        console.log("📊 KPI data updated:", {
            nodes: summary.totalNodes,
            healthy: summary.healthyNodes,
            avgLatency: summary.averageLatency,
            avgThroughput: summary.averageThroughput
        });

    } catch (error) {
        console.error("❌ Error fetching KPI data:", error);
        // Fallback to demo data if API is unavailable
        updateWithDemoData();
    }
}

function updateKPICards(summary) {
    // Update main KPI values
    document.getElementById('latency').innerText =
        summary.averageLatency ? `${summary.averageLatency} ms` : '-- ms';

    document.getElementById('throughput').innerText =
        summary.averageThroughput ? `${summary.averageThroughput} Mbps` : '-- Mbps';

    document.getElementById('errorRate').innerText =
        summary.averageErrorRate ? `${summary.averageErrorRate} %` : '-- %';

    // Add health indicators (you can add these to your HTML)
    updateHealthIndicators(summary);
}

function updateHealthIndicators(summary) {
    // Update or create health status elements
    const healthyNodes = summary.healthyNodes || 0;
    const totalNodes = summary.totalNodes || 0;
    const healthPercentage = totalNodes > 0 ? Math.round((healthyNodes / totalNodes) * 100) : 0;

    // You can add these elements to your HTML if they don't exist
    const healthElement = document.getElementById('healthStatus');
    if (healthElement) {
        healthElement.innerText = `${healthyNodes}/${totalNodes} (${healthPercentage}%)`;
        healthElement.className = healthPercentage >= 90 ? 'status-good' :
            healthPercentage >= 70 ? 'status-warning' : 'status-critical';
    }
}

function updateChart(summary) {
    const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

    // Limit chart data points to last 20 measurements
    if (kpiChart.data.labels.length > 20) {
        kpiChart.data.labels.shift();
        kpiChart.data.datasets.forEach(ds => ds.data.shift());
    }

    // Add new data points
    kpiChart.data.labels.push(time);
    kpiChart.data.datasets[0].data.push(summary.averageLatency || 0);
    kpiChart.data.datasets[1].data.push(summary.averageThroughput || 0);
    kpiChart.data.datasets[2].data.push(summary.averageErrorRate || 0);

    kpiChart.update('none'); // No animation for real-time updates
}

function updateSystemStatus(summary, realtimeData) {
    lastUpdateTime = new Date();

    // Update last updated time display
    const lastUpdateElement = document.getElementById('lastUpdate');
    if (lastUpdateElement) {
        lastUpdateElement.innerText = `Last updated: ${lastUpdateTime.toLocaleTimeString()}`;
    }

    // Update connection status
    const statusElement = document.getElementById('connectionStatus');
    if (statusElement) {
        statusElement.innerText = '🟢 Connected';
        statusElement.className = 'status-connected';
    }
}

function updateWithDemoData() {
    console.log("🔄 Using demo data - API unavailable");

    // Fallback demo data
    const demoLatency = parseFloat((20 + Math.random() * 30).toFixed(2));
    const demoThroughput = parseFloat((90 + Math.random() * 40).toFixed(2));
    const demoErrorRate = parseFloat((Math.random() * 1).toFixed(3));

    document.getElementById('latency').innerText = `${demoLatency} ms`;
    document.getElementById('throughput').innerText = `${demoThroughput} Mbps`;
    document.getElementById('errorRate').innerText = `${demoErrorRate} %`;

    // Update chart with demo data
    const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

    if (kpiChart.data.labels.length > 20) {
        kpiChart.data.labels.shift();
        kpiChart.data.datasets.forEach(ds => ds.data.shift());
    }

    kpiChart.data.labels.push(time);
    kpiChart.data.datasets[0].data.push(demoLatency);
    kpiChart.data.datasets[1].data.push(demoThroughput);
    kpiChart.data.datasets[2].data.push(demoErrorRate);

    kpiChart.update('none');

    // Update status to show demo mode
    const statusElement = document.getElementById('connectionStatus');
    if (statusElement) {
        statusElement.innerText = '🟡 Demo Mode';
        statusElement.className = 'status-demo';
    }
}

// Export for manual testing
window.updateKPIData = updateKPIData;