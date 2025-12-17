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
    // Approve buttons
    const approveButtons = document.querySelectorAll('.btn-approve');
    approveButtons.forEach(btn => {
        btn.addEventListener('click', handleApprove);
    });

    // Reject buttons
    const rejectButtons = document.querySelectorAll('.btn-reject');
    rejectButtons.forEach(btn => {
        btn.addEventListener('click', handleReject);
    });

    // View details buttons
    const viewButtons = document.querySelectorAll('.btn-view-details');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', handleViewDetails);
    });
}

function handleApprove(e) {
    const row = e.target.closest('tr');
    const driverName = row.cells[0].textContent;
    const phone = row.cells[1].textContent;
    const vehicleType = row.cells[2].textContent;
    const license = row.cells[3].textContent;

    if (confirm(`Approve driver registration for ${driverName}?`)) {
        const driverData = {
            name: driverName,
            phone: phone,
            vehicle: vehicleType,
            license: license,
            status: 'active'
        };

        row.style.transition = 'all 0.3s ease-out';
        row.style.opacity = '0';
        row.style.transform = 'translateX(100px)';

        setTimeout(() => {
            row.remove();
            addDriverToActiveList(driverData);
            showNotification(`✅ ${driverName} approved and moved to active!`, 'success');
            updatePendingBadge();
        }, 300);
    }
}

function handleReject(e) {
    const row = e.target.closest('tr');
    const driverName = row.cells[0].textContent;

    if (confirm(`Reject driver registration for ${driverName}?`)) {
        // Animate row out
        row.style.transition = 'all 0.3s ease-out';
        row.style.opacity = '0';
        row.style.transform = 'translateX(-100px)';

        setTimeout(() => {
            row.remove();
            showNotification(`❌ ${driverName} has been rejected.`, 'error');
            updatePendingBadge();
        }, 300);
    }
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
        // This is a driver card
        const driverData = {
            name: card.querySelector('h4').textContent,
            vehicle: card.querySelector('.driver-vehicle').textContent,
            status: card.querySelector('.driver-status').textContent,
            phone: card.getAttribute('data-driver-phone'),
            license: card.getAttribute('data-driver-license')
        };
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
    const rowCount = table ? table.rows.length : 0;

    if (badge) {
        badge.textContent = `${rowCount} Pending`;

        if (rowCount === 0) {
            badge.style.background = '#10B981';
            badge.textContent = 'All Clear ✓';
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


function loadMockData() {
    // This would come from Firebase in production
    console.log('📊 Loading mock data...');

    // Simulate data loading
    setTimeout(() => {
        console.log('✅ Mock data loaded successfully');
    }, 500);
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
