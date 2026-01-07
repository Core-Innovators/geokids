// Handle View Application button click
async function handleViewApplication(e) {
    const row = e.target.closest('tr');
    const driverId = row.getAttribute('data-driver-id');

    try {
        // Fetch full driver details
        const driver = await window.FirebaseService.getDriverById(driverId);
        showApplicationReviewModal(driver);
    } catch (error) {
        console.error('Error loading application:', error);
        showNotification('Failed to load application details', 'error');
    }
}

// Show application review modal for pending drivers
function showApplicationReviewModal(driver) {
    // Remove existing modal if any
    const existingModal = document.getElementById('application-review-modal');
    if (existingModal) existingModal.remove();

    // Ensure modal styles are loaded (defined in driver-management.js)
    if (typeof addModalStyles === 'function') {
        addModalStyles();
    }

    const routeData = driver.routeData || {};
    const distance = routeData.distance || 'N/A';
    const duration = routeData.duration || 'N/A';
    const startPoint = routeData.startPoint || { lat: 0, lng: 0 };
    const endPoint = routeData.endPoint || { lat: 0, lng: 0 };
    const pathCoordinates = routeData.pathCoordinates || [];

    const modal = document.createElement('div');
    modal.id = 'application-review-modal';
    modal.className = 'driver-modal-overlay';
    modal.innerHTML = `
        <div class="modal-content-large">
            <!-- Orange Header -->
            <div class="modal-header-orange">
                <h2><i class="fas fa-file-alt"></i> Driver Application Review</h2>
                <button id="close-review-modal" class="modal-close-btn">&times;</button>
            </div>
            
            <div class="modal-body-scroll">
                <!-- Status Badge -->
                <div style="background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%); padding: 1rem; border-radius: 0.75rem; margin-bottom: 2rem; border-left: 4px solid #F59E0B;">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <i class="fas fa-clock" style="color: #D97706; font-size: 1.25rem;"></i>
                        <span style="color: #92400E; font-weight: 600;">Pending Review</span>
                    </div>
                </div>

                <!-- Profile Section -->
                <div class="driver-profile-section">
                    <div class="profile-image-section">
                        <img src="${driver.profileImageUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(driver.fullName || 'Driver')}&size=200&background=FF6B3D&color=fff&bold=true`}" 
                             alt="Profile" class="driver-profile-img" 
                             onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(driver.fullName || 'Driver')}&size=200&background=FF6B3D&color=fff&bold=true'">
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${driver.fullName || 'N/A'}</h3>
                        
                        <div class="contact-grid">
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-phone"></i> Phone</p>
                                <p class="value">${driver.contactNumber || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-id-card"></i> NIC</p>
                                <p class="value">${driver.nic || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-map-marker-alt"></i> Address</p>
                                <p class="value">${driver.address || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-birthday-cake"></i> Birthday</p>
                                <p class="value">${driver.birthday || 'N/A'}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- License Images Section -->
                <div class="license-section">
                    <h4><i class="fas fa-id-card"></i> Driving License</h4>
                    <div class="license-grid">
                        <div class="license-item">
                            <h5>Front Side</h5>
                            <img src="${driver.frontLicenseUrl || 'https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Front+License'}" 
                                 alt="Front License" 
                                 class="license-img clickable-img"
                                 onclick="window.open(this.src, '_blank')"
                                 onerror="this.src='https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Front+License'">
                        </div>
                        <div class="license-item">
                            <h5>Back Side</h5>
                            <img src="${driver.backLicenseUrl || 'https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Back+License'}" 
                                 alt="Back License" 
                                 class="license-img clickable-img"
                                 onclick="window.open(this.src, '_blank')"
                                 onerror="this.src='https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Back+License'">
                        </div>
                    </div>
                </div>
                
                <!-- Route Information Section -->
                <div class="route-section">
                    <h4><i class="fas fa-route"></i> Route Information</h4>
                    <div class="route-stats">
                        <div class="route-stat-item">
                            <i class="fas fa-road"></i>
                            <div>
                                <p class="stat-label">Distance</p>
                                <p class="stat-value">${distance}</p>
                            </div>
                        </div>
                        <div class="route-stat-item">
                            <i class="fas fa-clock"></i>
                            <div>
                                <p class="stat-label">Duration</p>
                                <p class="stat-value">${duration}</p>
                            </div>
                        </div>
                        <div class="route-stat-item">
                            <i class="fas fa-map-pin"></i>
                            <div>
                                <p class="stat-label">Waypoints</p>
                                <p class="stat-value">${pathCoordinates.length} points</p>
                            </div>
                        </div>
                    </div>
                    <div class="route-coordinates">
                        <div class="coordinate-item">
                            <strong>Start:</strong> ${startPoint.lat.toFixed(6)}, ${startPoint.lng.toFixed(6)}
                        </div>
                        <div class="coordinate-item">
                            <strong>End:</strong> ${endPoint.lat.toFixed(6)}, ${endPoint.lng.toFixed(6)}
                        </div>
                    </div>
                </div>

                <!-- Vehicle Images Section -->
                ${(driver.vehicleImageUrls && driver.vehicleImageUrls.length > 0) ? `
                    <div class="vehicle-section">
                        <h4><i class="fas fa-car"></i> Vehicle Images</h4>
                        ${driver.vehicleImageUrls.length > 1 ? `
                            <div class="vehicle-carousel">
                                ${driver.vehicleImageUrls.map((url, index) => `
                                    <div class="vehicle-carousel-item ${index === 0 ? 'active' : ''}">
                                        <img src="${url}" 
                                             alt="Vehicle ${index + 1}" 
                                             class="vehicle-carousel-img clickable-img"
                                             onclick="window.open(this.src, '_blank')"
                                             onerror="this.src='https://via.placeholder.com/400x300/E5E7EB/6B7280?text=Vehicle+Image'">
                                    </div>
                                `).join('')}
                                <div class="carousel-controls">
                                    <button class="carousel-btn prev-btn" onclick="changeVehicleImage(-1)">
                                        <i class="fas fa-chevron-left"></i>
                                    </button>
                                    <div class="carousel-dots">
                                        ${driver.vehicleImageUrls.map((_, index) => `
                                            <span class="dot ${index === 0 ? 'active' : ''}" onclick="setVehicleImage(${index})"></span>
                                        `).join('')}
                                    </div>
                                    <button class="carousel-btn next-btn" onclick="changeVehicleImage(1)">
                                        <i class="fas fa-chevron-right"></i>
                                    </button>
                                </div>
                            </div>
                        ` : `
                            <img src="${driver.vehicleImageUrls[0]}" 
                                 alt="Vehicle" 
                                 class="vehicle-img clickable-img"
                                 onclick="window.open(this.src, '_blank')"
                                 onerror="this.src='https://via.placeholder.com/400x300/E5E7EB/6B7280?text=Vehicle+Image'">
                        `}
                    </div>
                ` : ''}
                
                <!-- Action Buttons -->
                <div class="modal-actions" style="border-top: 2px solid #E5E7EB; padding-top: 1.5rem; margin-top: 2rem;">
                    <button class="btn-modal btn-close-modal" style="background: #64748B;">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-reject-application" style="background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);">
                        <i class="fas fa-ban"></i> Reject Application
                    </button>
                    <button class="btn-modal btn-approve-application" style="background: linear-gradient(135deg, #10B981 0%, #059669 100%);">
                        <i class="fas fa-check-circle"></i> Approve Application
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Event handlers
    const closeModal = () => {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    };

    modal.querySelector('#close-review-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-close-modal').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target.classList.contains('driver-modal-overlay')) closeModal();
    });

    // Approve button
    modal.querySelector('.btn-approve-application').addEventListener('click', async () => {
        if (confirm(`Approve driver registration for ${driver.fullName}?`)) {
            try {
                closeModal();
                showNotification('Processing approval...', 'info');

                await window.FirebaseService.approveDriver(driver.id);
                const updatedDriver = await window.FirebaseService.getDriverById(driver.id);

                // Remove from pending table
                const row = document.querySelector(`tr[data-driver-id="${driver.id}"]`);
                if (row) {
                    row.style.transition = 'all 0.3s ease-out';
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(100px)';
                    setTimeout(() => {
                        row.remove();
                        updatePendingBadge();
                    }, 300);
                }

                // Add to active drivers
                window.DriverManagement.addDriverToActiveList(updatedDriver);
                showNotification(`✅ ${driver.fullName} approved and moved to active drivers!`, 'success');
            } catch (error) {
                console.error('Error approving driver:', error);
                showNotification('Failed to approve driver. Please try again.', 'error');
            }
        }
    });

    // Reject button
    modal.querySelector('.btn-reject-application').addEventListener('click', () => {
        closeModal();
        // Find the row to pass to rejection modal
        const row = document.querySelector(`tr[data-driver-id="${driver.id}"]`);
        if (row) {
            showRejectionReasonModal(driver.id, driver.fullName, row);
        }
    });
}
