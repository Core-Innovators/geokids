// Check if user is logged in, redirect to login if not
if (localStorage.getItem('isAdminLoggedIn') !== 'true') {
    window.location.href = 'login.html';
}

// App State
const appState = {
    currentPage: 'dashboard',
    userData: {
        username: localStorage.getItem('adminUsername') || 'admin',
        loginTime: localStorage.getItem('loginTime')
    },
    isAuthenticated: true
};

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    initializeNavigation();
    initializeDashboardCards();
    initializeButtons();
    loadMockData();
    displayUserInfo();

    console.log('✅ GeoKids Admin Dashboard Initialized');
    console.log(`👤 Logged in as: ${appState.userData.username}`);
});


function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();

            // Handle Sign Out
            if (item.id === 'nav-signout') {
                handleSignOut();
                return;
            }

            const page = item.getAttribute('data-page');
            if (page) {
                navigateToPage(page);
            }
        });
    });
}

function navigateToPage(pageName) {
    // Update state
    appState.currentPage = pageName;

    // Hide all pages
    const allPages = document.querySelectorAll('.page-content');
    allPages.forEach(page => {
        page.classList.remove('active');
    });

    // Show selected page
    const selectedPage = document.getElementById(`page-${pageName}`);
    if (selectedPage) {
        selectedPage.classList.add('active');
    }

    // Update sidebar active state
    const allNavItems = document.querySelectorAll('.nav-item');
    allNavItems.forEach(item => {
        item.classList.remove('active');
    });

    const activeNavItem = document.getElementById(`nav-${pageName}`);
    if (activeNavItem) {
        activeNavItem.classList.add('active');
    }

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });

    console.log(`📄 Navigated to: ${pageName}`);
}


function initializeDashboardCards() {
    const dashboardCards = document.querySelectorAll('.dashboard-card');

    dashboardCards.forEach(card => {
        card.addEventListener('click', () => {
            const navigateTo = card.getAttribute('data-navigate');
            if (navigateTo) {
                navigateToPage(navigateTo);
            }
        });

        // Add ripple effect
        card.addEventListener('mousedown', createRipple);
    });
}

function createRipple(e) {
    const card = e.currentTarget;
    const ripple = document.createElement('div');

    const rect = card.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    ripple.style.cssText = `
        position: absolute;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        transform: translate(-50%, -50%) scale(0);
        animation: ripple 0.6s ease-out;
        pointer-events: none;
        left: ${x}px;
        top: ${y}px;
    `;

    card.appendChild(ripple);

    setTimeout(() => ripple.remove(), 600);
}

// Add ripple animation to CSS dynamically
const style = document.createElement('style');
style.textContent = `
    @keyframes ripple {
        to {
            transform: translate(-50%, -50%) scale(20);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);


function initializeButtons() {
    // View Application buttons (for pending drivers)
    const viewAppButtons = document.querySelectorAll('.btn-view-application');
    viewAppButtons.forEach(btn => {
        btn.addEventListener('click', handleViewApplication);
    });

    // View details buttons (for active drivers)
    const viewButtons = document.querySelectorAll('.btn-view-details');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', handleViewDetails);
    });
}

async function handleApprove(e) {
    const row = e.target.closest('tr');
    const driverId = row.getAttribute('data-driver-id');
    const driverName = row.cells[0].textContent;

    if (confirm(`Approve driver registration for ${driverName}?`)) {
        try {
            // Show loading
            showNotification('Processing approval...', 'info');

            // Update Firebase status to approved
            await window.FirebaseService.approveDriver(driverId);

            // Fetch the updated driver details
            const driver = await window.FirebaseService.getDriverById(driverId);

            // Animate out from pending table
            row.style.transition = 'all 0.3s ease-out';
            row.style.opacity = '0';
            row.style.transform = 'translateX(100px)';

            setTimeout(() => {
                row.remove();

                // Add to active drivers grid
                window.DriverManagement.addDriverToActiveList(driver);

                showNotification(`✅ ${driverName} approved and moved to active drivers!`, 'success');
                updatePendingBadge();
            }, 300);
        } catch (error) {
            console.error('Error approving driver:', error);
            showNotification('Failed to approve driver. Please try again.', 'error');
        }
    }
}

function handleReject(e) {
    const row = e.target.closest('tr');
    const driverId = row.getAttribute('data-driver-id');
    const driverName = row.cells[0].textContent;

    // Show rejection reason modal
    showRejectionReasonModal(driverId, driverName, row);
}

// Show modal to get rejection reason
function showRejectionReasonModal(driverId, driverName, row) {
    // Remove existing modal if any
    const existingModal = document.getElementById('rejection-modal');
    if (existingModal) existingModal.remove();

    const modal = document.createElement('div');
    modal.id = 'rejection-modal';
    modal.className = 'driver-modal-overlay';
    modal.innerHTML = `
        <div class="modal-content-medium">
            <div class="modal-header-orange">
                <h2><i class="fas fa-times-circle"></i> Reject Application</h2>
                <button class="modal-close-btn" onclick="this.closest('.driver-modal-overlay').remove()">&times;</button>
            </div>
            <div class="modal-body">
                <p style="margin-bottom: 1rem; color: #64748B;">Why are you rejecting <strong>${driverName}</strong>'s application?</p>
                <textarea id="rejection-reason" 
                          placeholder="Enter rejection reason (required)" 
                          style="width: 100%; min-height: 100px; padding: 0.75rem; border: 2px solid #E2E8F0; border-radius: 0.5rem; font-family: inherit; resize: vertical;"
                          required></textarea>
                <div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
                    <button class="btn-modal btn-cancel" onclick="this.closest('.driver-modal-overlay').remove()" style="flex: 1; background: #64748B; color: white; padding: 0.75rem; border: none; border-radius: 0.5rem; font-weight: 600; cursor: pointer;">
                        Cancel
                    </button>
                    <button class="btn-modal btn-submit-reject" style="flex: 1; background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%); color: white; padding: 0.75rem; border: none; border-radius: 0.5rem; font-weight: 600; cursor: pointer;">
                        <i class="fas fa-ban"></i> Submit Rejection
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Handle submit
    modal.querySelector('.btn-submit-reject').addEventListener('click', async () => {
        const reason = document.getElementById('rejection-reason').value.trim();

        if (!reason) {
            alert('Please enter a rejection reason');
            return;
        }

        try {
            modal.remove();
            showNotification('Processing rejection...', 'info');

            // Update Firebase
            await window.FirebaseService.rejectDriver(driverId, reason);

            // Animate row out
            row.style.transition = 'all 0.3s ease-out';
            row.style.opacity = '0';
            row.style.transform = 'translateX(-100px)';

            setTimeout(() => {
                row.remove();
                showNotification(`❌ ${driverName}'s application has been rejected.`, 'warning');
                updatePendingBadge();
            }, 300);
        } catch (error) {
            console.error('Error rejecting driver:', error);
            showNotification('Failed to reject driver. Please try again.', 'error');
        }
    });
}

function handleViewDetails(e) {
    const card = e.target.closest('.driver-card');
    const row = e.target.closest('tr');

    console.log('=== handleViewDetails DEBUG ===');
    console.log('Card:', card);
    console.log('Row:', row);
    if (row) {
        console.log('Row cells length:', row.cells.length);
        console.log('Cell [2] content:', row.cells[2]?.textContent);
        console.log('Has @ symbol:', row.cells[2]?.textContent.includes('@'));
    }

    // Check if it's from Parents page (has email column)
    if (row && row.cells.length >= 6 && row.cells[2].textContent.includes('@')) {
        console.log('✅ DETECTED AS PARENT');
        // This is a parent row
        const parentData = {
            name: row.cells[0].textContent,
            phone: row.cells[1].textContent,
            email: row.cells[2].textContent,
            children: row.cells[3].textContent,
            joined: row.cells[4].textContent
        };
        console.log('Parent data:', parentData);
        console.log('Calling showParentDetailsModal...');
        showParentDetailsModal(parentData);
    } else if (card) {
        console.log('✅ DETECTED AS DRIVER CARD');
        // This is a driver card - extract driver ID
        const driverId = card.getAttribute('data-driver-id');
        const driverData = {
            id: driverId, // Pass the Firebase document ID
            name: card.querySelector('h4')?.textContent,
            vehicle: card.querySelector('.driver-vehicle')?.textContent,
            status: card.querySelector('.driver-status')?.textContent,
            phone: card.getAttribute('data-driver-phone')
        };
        console.log('Driver data with ID:', driverData);
        showDriverDetailsModal(driverData);
    } else if (row) {
        console.log('✅ DETECTED AS DRIVER ROW (fallback)');
        // Fallback for driver rows in pending table
        const driverData = {
            name: row.cells[0].textContent,
            phone: row.cells[1].textContent,
            vehicle: row.cells[2].textContent,
            license: row.cells[3].textContent
        };
        showDriverDetailsModal(driverData);
    }
    console.log('=== END DEBUG ===');
}

function handleSignOut() {
    if (confirm('Are you sure you want to sign out?')) {
        showNotification('👋 Signing out...', 'info');

        setTimeout(() => {
            // Clear session data
            localStorage.removeItem('isAdminLoggedIn');
            localStorage.removeItem('adminUsername');
            localStorage.removeItem('loginTime');
            localStorage.removeItem('rememberMe');

            appState.isAuthenticated = false;
            appState.userData = null;

            // Redirect to login page
            window.location.href = 'login.html';
        }, 100);
    }
}

function updatePendingBadge() {
    const badge = document.querySelector('.badge');
    const table = document.getElementById('pending-drivers-table');
    // Count only rows with data-driver-id (actual driver rows, not the "no pending" message)
    const rowCount = table ? table.querySelectorAll('tr[data-driver-id]').length : 0;

    if (badge) {
        badge.textContent = `${rowCount} Pending`;

        if (rowCount === 0) {
            badge.style.background = '#10B981';
            badge.textContent = '0 Pending';
        }
    }
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;

    // Style based on type
    const colors = {
        success: '#10B981',
        error: '#EF4444',
        info: '#3B82F6',
        warning: '#F59E0B'
    };

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${colors[type] || colors.info};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 0.75rem;
        box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        z-index: 9999;
        animation: slideInRight 0.3s ease-out;
        font-weight: 600;
        max-width: 400px;
    `;

    document.body.appendChild(notification);

    // Auto remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Add notification animations
const notificationStyle = document.createElement('style');
notificationStyle.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(notificationStyle);


async function loadMockData() {
    console.log('📊 Loading driver data from Firebase...');

    try {
        // Load pending and active drivers from Firebase
        const [pendingDrivers, activeDrivers] = await Promise.all([
            window.FirebaseService.getPendingDrivers(),
            window.FirebaseService.getActiveDrivers()
        ]);

        console.log(`✅ Loaded ${pendingDrivers.length} pending and ${activeDrivers.length} active drivers`);

        // Populate pending drivers table
        const pendingTable = document.getElementById('pending-drivers-table');
        if (pendingTable && pendingDrivers.length > 0) {
            pendingTable.innerHTML = '';

            pendingDrivers.forEach(driver => {
                const row = document.createElement('tr');
                row.setAttribute('data-driver-id', driver.id);

                // Format time ago
                const createdDate = new Date(driver.createdAt);
                const timeAgo = getTimeAgo(createdDate);

                row.innerHTML = `
                    <td>${driver.fullName || 'N/A'}</td>
                    <td>${driver.contactNumber || 'N/A'}</td>
                    <td>${driver.address || 'N/A'}</td>
                    <td>${driver.nic || 'N/A'}</td>
                    <td>${timeAgo}</td>
                    <td>
                        <button class="btn-view-application">View Application</button>
                    </td>
                `;

                pendingTable.appendChild(row);
            });

            // Re-initialize button handlers
            initializeButtons();
            updatePendingBadge();
        } else if (pendingTable) {
            pendingTable.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #94A3B8;">No pending driver applications</td></tr>';
            updatePendingBadge();
        }

        // Populate the active drivers grid
        const driversGrid = document.querySelector('.drivers-grid');
        if (driversGrid) {
            driversGrid.innerHTML = '';

            if (activeDrivers.length > 0) {
                activeDrivers.forEach(driver => {
                    window.DriverManagement.addDriverToActiveList(driver);
                });
            } else {
                driversGrid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #94A3B8;">No active drivers yet</p>';
            }
        }

        // Load children data
        await loadChildrenData();

        // Load parents data
        await loadParentsData();

    } catch (error) {
        console.error('❌ Error loading drivers from Firebase:', error);
        showNotification('Failed to load drivers. Please check console.', 'error');
    }
}

// Load children data from Firebase
async function loadChildrenData() {
    console.log('👶 Loading children data from Firebase...');

    const childrenTableBody = document.getElementById('children-table-body');
    if (!childrenTableBody) return;

    try {
        const children = await window.FirebaseService.getRegisteredChildren();
        console.log(`✅ Loaded ${children.length} registered children`);

        if (children.length > 0) {
            childrenTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #94A3B8;"><i class="fas fa-spinner fa-spin" style="margin-right: 0.5rem;"></i>Loading parent details...</td></tr>';

            // Fetch parent data for each child in parallel
            const childrenWithParents = await Promise.all(children.map(async (child) => {
                // If parentContact1 is missing, try to fetch from parents collection
                if (!child.parentContact1) {
                    const parentRef = child.parentId || child.userId || child.parentUserId;
                    if (parentRef) {
                        try {
                            // Try getting parent by document ID first
                            let parentData = await window.FirebaseService.getParentById(parentRef);

                            // If not found, query by parentId field
                            if (!parentData) {
                                const snapshot = await db.collection('parents')
                                    .where('parentId', '==', parentRef)
                                    .limit(1)
                                    .get();

                                if (!snapshot.empty) {
                                    const doc = snapshot.docs[0];
                                    parentData = { id: doc.id, ...doc.data() };
                                }
                            }

                            if (parentData) {
                                // Merge parent data into child
                                child.parentName = parentData.parentName || parentData.fullName || child.parentName;
                                child.parentContact1 = parentData.parentContact1 || parentData.contactNumber || parentData.phone;
                                child.parentContact2 = parentData.parentContact2 || parentData.secondaryContact;
                                child.parentNic = parentData.parentNic || parentData.nic;
                                // Also get pickup location
                                child.pickupAddress = child.pickupAddress || parentData.pickupAddress;
                                if (!child.pickupCoordinates && parentData.pickupCoordinates) {
                                    child.pickupCoordinates = parentData.pickupCoordinates;
                                }
                            }
                        } catch (err) {
                            console.warn('Could not fetch parent for child:', child.childName, err);
                        }
                    }
                }
                return child;
            }));

            childrenTableBody.innerHTML = '';

            childrenWithParents.forEach(child => {
                const row = document.createElement('tr');
                row.setAttribute('data-child-id', child.id);

                // Store child data for modal
                row.setAttribute('data-child-data', JSON.stringify(child));

                row.innerHTML = `
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                            <div style="width: 40px; height: 40px; background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600;">
                                ${(child.childName || 'C').charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <div style="font-weight: 600; color: #1E293B;">${child.childName || 'N/A'}</div>
                                <div style="font-size: 0.75rem; color: #94A3B8;">${child.childAge || 'N/A'} years old</div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <a href="tel:${child.parentContact1 || ''}" style="color: #3B82F6; text-decoration: none; font-weight: 500;">
                            ${child.parentContact1 || 'N/A'}
                        </a>
                    </td>
                    <td>${child.childSchool || 'N/A'}</td>
                    <td>
                        <span style="background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%); color: white; padding: 0.25rem 0.75rem; border-radius: 1rem; font-size: 0.75rem; font-weight: 600;">
                            ${child.childGrade || 'N/A'}
                        </span>
                    </td>
                    <td>
                        <button class="btn-track-child" style="background: linear-gradient(135deg, #10B981 0%, #059669 100%); color: white; border: none; padding: 0.5rem 1rem; border-radius: 0.5rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease;">
                            <i class="fas fa-map-marker-alt"></i> Track
                        </button>
                    </td>
                    <td>
                        <button class="btn-view-child" style="background: linear-gradient(135deg, #F97316 0%, #EA580C 100%); color: white; border: none; padding: 0.5rem 1rem; border-radius: 0.5rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease;">
                            <i class="fas fa-eye"></i> View
                        </button>
                    </td>
                `;

                childrenTableBody.appendChild(row);
            });

            // Add event listeners for View buttons
            initializeChildrenButtons();

        } else {
            childrenTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #94A3B8;"><i class="fas fa-child" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>No registered children found</td></tr>';
        }
    } catch (error) {
        console.error('❌ Error loading children from Firebase:', error);
        childrenTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #EF4444;"><i class="fas fa-exclamation-triangle" style="margin-right: 0.5rem;"></i>Failed to load children data</td></tr>';
    }
}

// Initialize children table buttons
function initializeChildrenButtons() {
    // View buttons
    const viewButtons = document.querySelectorAll('.btn-view-child');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const childDataStr = row.getAttribute('data-child-data');

            if (childDataStr) {
                try {
                    const childData = JSON.parse(childDataStr);
                    console.log('👀 Viewing child details:', childData);
                    window.ParentManagement.showChildDetailsModal(childData);
                } catch (err) {
                    console.error('Error parsing child data:', err);
                    showNotification('Failed to load child details', 'error');
                }
            }
        });

        // Add hover effects
        btn.addEventListener('mouseenter', () => {
            btn.style.transform = 'translateY(-2px)';
            btn.style.boxShadow = '0 4px 12px rgba(249, 115, 22, 0.4)';
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'translateY(0)';
            btn.style.boxShadow = 'none';
        });
    });

    // Track buttons
    const trackButtons = document.querySelectorAll('.btn-track-child');
    trackButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            showNotification('🚧 Tracking feature coming soon!', 'info');
        });

        // Add hover effects
        btn.addEventListener('mouseenter', () => {
            btn.style.transform = 'translateY(-2px)';
            btn.style.boxShadow = '0 4px 12px rgba(16, 185, 129, 0.4)';
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'translateY(0)';
            btn.style.boxShadow = 'none';
        });
    });
}

// Load parents data from Firebase
async function loadParentsData() {
    console.log('👨‍👩‍👧 Loading parents data from Firebase...');

    const parentsTableBody = document.getElementById('parents-table-body');
    if (!parentsTableBody) return;

    try {
        // Fetch all parents from Firebase
        const parentsSnapshot = await db.collection('parents').get();
        const parents = parentsSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        console.log(`✅ Loaded ${parents.length} registered parents`);

        // Also fetch all children to count per parent
        const childrenSnapshot = await db.collection('children').get();
        const allChildren = childrenSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        if (parents.length > 0) {
            parentsTableBody.innerHTML = '';

            parents.forEach(parent => {
                const row = document.createElement('tr');
                row.setAttribute('data-parent-id', parent.id);

                // Count children for this parent
                const parentRef = parent.parentId || parent.id;
                const childrenCount = allChildren.filter(child =>
                    child.parentId === parentRef ||
                    child.userId === parentRef ||
                    child.parentUserId === parentRef
                ).length;

                // Store parent data for modal
                row.setAttribute('data-parent-data', JSON.stringify({
                    ...parent,
                    childrenCount
                }));

                // Truncate address if too long
                const address = parent.pickupAddress || 'Not Set';
                const truncatedAddress = address.length > 30 ? address.substring(0, 30) + '...' : address;

                row.innerHTML = `
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                            <div style="width: 40px; height: 40px; background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600;">
                                ${(parent.parentName || 'P').charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <div style="font-weight: 600; color: #1E293B;">${parent.parentName || 'N/A'}</div>
                                <div style="font-size: 0.75rem; color: #94A3B8;">Parent</div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <a href="tel:${parent.parentContact1 || ''}" style="color: #3B82F6; text-decoration: none; font-weight: 500;">
                            ${parent.parentContact1 || 'N/A'}
                        </a>
                    </td>
                    <td>${parent.parentNic || 'N/A'}</td>
                    <td>
                        <span style="background: ${childrenCount > 0 ? 'linear-gradient(135deg, #EC4899 0%, #DB2777 100%)' : '#94A3B8'}; color: white; padding: 0.25rem 0.75rem; border-radius: 1rem; font-size: 0.75rem; font-weight: 600;">
                            ${childrenCount} ${childrenCount === 1 ? 'Child' : 'Children'}
                        </span>
                    </td>
                    <td title="${address}" style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                        ${truncatedAddress}
                    </td>
                    <td>
                        <button class="btn-view-parent" style="background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%); color: white; border: none; padding: 0.5rem 1rem; border-radius: 0.5rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease;">
                            <i class="fas fa-eye"></i> View
                        </button>
                    </td>
                `;

                parentsTableBody.appendChild(row);
            });

            // Add event listeners for View buttons
            initializeParentsButtons();

        } else {
            parentsTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #94A3B8;"><i class="fas fa-users" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>No registered parents found</td></tr>';
        }
    } catch (error) {
        console.error('❌ Error loading parents from Firebase:', error);
        parentsTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: #EF4444;"><i class="fas fa-exclamation-triangle" style="margin-right: 0.5rem;"></i>Failed to load parents data</td></tr>';
    }
}

// Initialize parents table buttons
function initializeParentsButtons() {
    const viewButtons = document.querySelectorAll('.btn-view-parent');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const row = e.target.closest('tr');
            const parentDataStr = row.getAttribute('data-parent-data');

            if (parentDataStr) {
                try {
                    const parentData = JSON.parse(parentDataStr);
                    console.log('👀 Viewing parent details:', parentData);

                    // Fetch children for this parent
                    const parentRef = parentData.parentId || parentData.id;
                    const childrenSnapshot = await db.collection('children')
                        .get();

                    const children = childrenSnapshot.docs
                        .map(doc => ({ id: doc.id, ...doc.data() }))
                        .filter(child =>
                            child.parentId === parentRef ||
                            child.userId === parentRef ||
                            child.parentUserId === parentRef
                        );

                    parentData.children = children;
                    window.ParentManagement.showParentDetailsModal(parentData);
                } catch (err) {
                    console.error('Error parsing parent data:', err);
                    showNotification('Failed to load parent details', 'error');
                }
            }
        });

        // Add hover effects
        btn.addEventListener('mouseenter', () => {
            btn.style.transform = 'translateY(-2px)';
            btn.style.boxShadow = '0 4px 12px rgba(59, 130, 246, 0.4)';
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'translateY(0)';
            btn.style.boxShadow = 'none';
        });
    });
}

// Helper function to calculate time ago
function getTimeAgo(date) {
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 60) return `${diffMins} min ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
}


const searchInputs = document.querySelectorAll('.search-input');
searchInputs.forEach(input => {
    input.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        console.log(`🔍 Searching for: ${searchTerm}`);

        // In a real app, this would filter the data
        // For now, we'll just show a placeholder
        if (searchTerm.length > 2) {
            showNotification(`Searching for "${searchTerm}"...`, 'info');
        }
    });
});


let sidebarCollapsed = false;

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebarCollapsed = !sidebarCollapsed;

    if (sidebarCollapsed) {
        sidebar.style.transform = 'translateX(-100%)';
    } else {
        sidebar.style.transform = 'translateX(0)';
    }
}

// Add mobile menu button for small screens
if (window.innerWidth <= 768) {
    const menuButton = document.createElement('button');
    menuButton.className = 'mobile-menu-btn';
    menuButton.innerHTML = '<i class="fas fa-bars"></i>';
    menuButton.style.cssText = `
        position: fixed;
        top: 20px;
        left: 20px;
        z-index: 1001;
        background: var(--primary-orange);
        color: white;
        border: none;
        padding: 1rem;
        border-radius: 0.5rem;
        font-size: 1.25rem;
        cursor: pointer;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    `;

    menuButton.addEventListener('click', toggleSidebar);
    document.body.appendChild(menuButton);
}


function displayUserInfo() {
    const username = appState.userData.username;
    const loginTime = new Date(appState.userData.loginTime);

    // You can add this to the sidebar or header if desired
    console.log(`📊 Session Info:`);
    console.log(`   User: ${username}`);
    console.log(`   Login Time: ${loginTime.toLocaleString()}`);
}

// Session timeout check (optional - 24 hours)
function checkSessionTimeout() {
    const loginTime = new Date(appState.userData.loginTime);
    const now = new Date();
    const hoursSinceLogin = (now - loginTime) / (1000 * 60 * 60);

    // Auto logout after 24 hours
    if (hoursSinceLogin > 24 && localStorage.getItem('rememberMe') !== 'true') {
        showNotification('⏰ Session expired. Please login again.', 'warning');
        setTimeout(() => {
            localStorage.clear();
            window.location.href = 'login.html';
        }, 2000);
    }
}

// Check session every 30 minutes
setInterval(checkSessionTimeout, 30 * 60 * 1000);


window.GeoKidsApp = {
    navigateToPage,
    showNotification,
    appState,
    handleSignOut
};

console.log('🚀 GeoKids Dashboard Ready!');
