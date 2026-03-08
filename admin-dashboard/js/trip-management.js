/**
 * Trip Management Module
 * Handles fetching and displaying active and completed trips
 */

const TripManagement = (() => {
    // Selectors
    const activeTripsList = document.querySelector('.trips-list');
    const activeTripsCount = document.querySelector('.active-trips + .stat-info .stat-number');
    const completedTripsCount = document.querySelector('.completed-trips + .stat-info .stat-number');

    // Cache for driver data to avoid redundant fetches
    const driverCache = {};

    /**
     * Initialize Tripoli Management
     */
    async function init() {
        console.log('🚗 Initializing Trip Management...');
        await loadTripData();
    }

    /**
     * Fetch all relevant trip data
     */
    async function loadTripData() {
        try {
            // Get active and completed trips in parallel
            const [activeTrips, completedTrips] = await Promise.all([
                window.FirebaseService.getActiveTrips(),
                window.FirebaseService.getCompletedTrips()
            ]);

            console.log(`📊 Tripoli Summary: ${activeTrips.length} active, ${completedTrips.length} completed`);

            // Update Statistics
            if (activeTripsCount) activeTripsCount.textContent = activeTrips.length;
            if (completedTripsCount) completedTripsCount.textContent = completedTrips.length;

            // Render Active Trips
            await renderActiveTrips(activeTrips);

            // Render Completed Trips (Need a container)
            await renderCompletedTrips(completedTrips);

        } catch (error) {
            console.error('❌ Error loading trip data:', error);
        }
    }

    /**
     * Fetches driver name from cache or Firebase
     */
    async function getDriverName(driverId) {
        if (!driverId) return 'Unknown Driver';
        if (driverCache[driverId]) return driverCache[driverId];

        try {
            const driver = await window.FirebaseService.getDriverById(driverId);
            const name = driver.fullName || driver.name || 'Unknown Driver';
            driverCache[driverId] = name;
            return name;
        } catch (error) {
            console.warn(`Could not fetch driver ${driverId}:`, error);
            return 'Unknown Driver';
        }
    }

    /**
     * Helper to format relative time
     */
    function getTimeAgo(timestamp) {
        if (!timestamp) return 'N/A';
        const now = Date.now();
        const diff = now - timestamp;

        const mins = Math.floor(diff / 60000);
        if (mins < 1) return 'Just now';
        if (mins < 60) return `${mins} min ago`;

        const hours = Math.floor(mins / 60);
        if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;

        return new Date(timestamp).toLocaleDateString();
    }

    /**
     * Render active trips into the list
     */
    async function renderActiveTrips(trips) {
        if (!activeTripsList) return;

        if (trips.length === 0) {
            activeTripsList.innerHTML = `
                <div style="text-align: center; padding: 2rem; color: #94A3B8;">
                    <i class="fas fa-route" style="font-size: 2rem; display: block; margin-bottom: 0.5rem;"></i>
                    No active trips at the moment
                </div>
            `;
            return;
        }

        activeTripsList.innerHTML = '';

        // Use for...of to await driver names sequentially (or use Promise.all)
        const tripElements = await Promise.all(trips.map(async (trip) => {
            const driverName = await getDriverName(trip.driverId);
            const childCount = (trip.pickedUpChildren ? trip.pickedUpChildren.length : 0);
            const totalPending = (trip.pendingChildren ? trip.pendingChildren.length : 0);
            const startTime = trip.startTime || trip.createdAt || Date.now();

            return `
                <div class="trip-item">
                    <div class="trip-info">
                        <h4>Trip #${trip.id.substring(0, 8)}</h4>
                        <p><strong>Driver:</strong> ${driverName}</p>
                        <p><strong>Children:</strong> ${childCount} picked up / ${totalPending} remaining</p>
                    </div>
                    <div class="trip-status">
                        <span class="status-badge in-progress">In Progress</span>
                        <p class="trip-time">Started ${getTimeAgo(startTime)}</p>
                    </div>
                </div>
            `;
        }));

        activeTripsList.innerHTML = tripElements.join('');
    }

    /**
     * Render completed trips into a new section
     */
    async function renderCompletedTrips(trips) {
        // Find or create completed trips container
        let completedContainer = document.getElementById('completed-trips-list');
        if (!completedContainer) {
            // Create the section in HTML dynamically if not present
            const tripsPage = document.getElementById('page-trips');
            if (!tripsPage) return;

            const section = document.createElement('div');
            section.className = 'content-section';
            section.style.marginTop = '2rem';
            section.innerHTML = `
                <div class="section-header">
                    <h3>Completed Rides</h3>
                </div>
                <div id="completed-trips-list" class="trips-list"></div>
            `;
            tripsPage.appendChild(section);
            completedContainer = document.getElementById('completed-trips-list');
        }

        if (trips.length === 0) {
            completedContainer.innerHTML = '<p style="text-align: center; padding: 1rem; color: #94A3B8;">No completed rides found</p>';
            return;
        }

        completedContainer.innerHTML = '';

        const tripElements = await Promise.all(trips.map(async (trip) => {
            const driverName = await getDriverName(trip.driverId);
            const childCount = (trip.pickedUpChildren ? trip.pickedUpChildren.length : 0);
            const endTime = trip.endTime || trip.completedAt;

            return `
                <div class="trip-item" style="border-left: 4px solid #10B981;">
                    <div class="trip-info">
                        <h4>Trip #${trip.id.substring(0, 8)}</h4>
                        <p><strong>Driver:</strong> ${driverName}</p>
                        <p><strong>Children:</strong> ${childCount} completed pickup</p>
                    </div>
                    <div class="trip-status">
                        <span class="status-badge" style="background: #D1FAE5; color: #065F46;">Completed</span>
                        <p class="trip-time">${endTime ? new Date(endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'N/A'}</p>
                    </div>
                </div>
            `;
        }));

        completedContainer.innerHTML = tripElements.join('');
    }

    return {
        init,
        loadTripData
    };
})();

// Export to window
window.TripManagement = TripManagement;
