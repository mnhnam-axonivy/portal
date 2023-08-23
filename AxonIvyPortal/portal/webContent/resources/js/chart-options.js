const pathName = window.location.pathname;
const baseURL = pathName.substring(0, pathName.indexOf('/faces/'));
const instance =  axios.create({
        baseURL: baseURL,
        timeout: 60000,
        headers: {'X-Requested-By': 'ivy'}
    });
const CHART_COLORS = [
    'hsl(192, 63%, 70%)',
    'hsl(192, 63%, 60%)',
    'hsl(192, 63%, 50%)',
    'hsl(192, 63%, 40%)',
    'hsl(192, 63%, 30%)',
    'hsl(192, 63%, 20%)',
    'hsl(192, 63%, 10%)',
];

const PIE_COLORS = [
    'rgb(255, 99, 132)',
    'rgb(54, 162, 235)',
    'rgb(255, 205, 86)',
    'rgb(255, 105, 86)'
]

function isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

function formatISODate(dt) {
    let year = dt.getFullYear();
    let month = dt.getMonth() < 10 ? '0' + dt.getMonth() : dt.getMonth();
    let date = dt.getDate() < 10 ? '0' + dt.getDate() : dt.getDate();
    return `${year}-${month}-${date}`;
}

$(document).ready(function () {
    let refreshInfos = [];
    $('.chart-options').each(async (index, chart) => {
        let chartId = chart.getAttribute('data-chart-id');
        let response = await instance.post('/api/statistic-data-service/Data', {"chartId": chartId});
        let refreshInterval = chart.getAttribute('refresh-interval');

        let data = await response.data;
        let result = data.result.aggs[0].buckets;
        let chartType = data.chartType;
        let chartObject;
        if ('number' === chartType) {
            let html = renderNumberChart(data.label);
            $(chart).html(html);
            $(chart).find('.card-number').text(result.map(bucket => bucket.count));
        }
        if ('bar' === chartType) {
            let html = renderBarChart(chartId);
            $(chart).html(html);
            let canvasObject = $(chart).find('canvas');
            chartObject = new Chart(canvasObject, {
                type: chartType,
                label: data.label,
                data: {
                    labels: result.map(bucket => formatChartLabel(bucket.key)),
                    datasets: [{
                        label: data.label,
                        data: result.map(bucket => bucket.count),
                        backgroundColor: CHART_COLORS
                    }
                    ]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                text: data.barChartConfig.yTitle,
                                display: true
                            }
                        },
                        x: {
                            title: {
                                text: data.barChartConfig.xTitle,
                                display: true
                            }
                        }
                    }
                }
            });

        }
        if ('pie' === chartType || 'doughnut' === chartType) {
            let html = renderBarChart(chartId);
            $(chart).html(html);
            let canvasObject = $(chart).find('canvas');
            chartObject = new Chart(canvasObject, {
                type: chartType,
                label: data.label,
                data: {
                    labels: result.map(bucket => formatChartLabel(bucket.key)),
                    datasets: [{
                        label: data.label,
                        data: result.map(bucket => bucket.count),
                        backgroundColor: PIE_COLORS
                    }
                    ],
                    hoverOffset: 4
                }
            });

        }
        if (chartObject !== undefined) {
            refreshInfos.push({'chartObject': chartObject, 'chartType': chartType, 'chartId': chartId, 'refreshInterval': refreshInterval});
        }
        if ($('.chart-options').length === refreshInfos.length) {
            initRefresh(refreshInfos);
        }
    });

    function formatChartLabel(label) {
        if (isNumeric((new Date(label)).getTime())) {
            return formatISODate(new Date(label));
        }
        return label
    }

    function initRefresh(refreshInfos) {
        for (let i = 0; i < refreshInfos.length; i++) {
            let refreshInfo = refreshInfos[i];
            if (refreshInfo.refreshInterval && refreshInfo.refreshInterval > 0) {
                setInterval(() => {
                    refreshChart(refreshInfo.chartObject, refreshInfo.chartId);
                }, refreshInfo.refreshInterval * 1000);
            }
        }
    }

    async function refreshChart(chartObject, chartId) {
        const response = await instance.post('/api/statistic-data-service/Data', {"chartId": chartId});
        const result = response.data.result.aggs[0].buckets;
        chartObject.data.labels = result.map(bucket => formatChartLabel(bucket.key));
        chartObject.data.datasets.forEach(dataset => {
            dataset.data = result.map(bucket => bucket.count);
        });
        chartObject.update();
    }

    const renderBarChart = (chartId) => {
      let html = `<div style="max-height: 600px; max-width: 600px;">
              <canvas id="${chartId}" width="600" height="600"></canvas>
            </div>`;
      return html;
    };

    const renderNumberChart = (label) => {
        let html = `
         <div style="height: 150px; max-width: 300px; background-color: #2980b9; color: white;" class="card">
              <h4 class="card-name">${label}</h4>
              <h1 class="card-number"></h1>
            </div>`;
        return html;
    };
});