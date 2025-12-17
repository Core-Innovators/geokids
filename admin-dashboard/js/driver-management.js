
// Add approved driver to active drivers list
function addDriverToActiveList(driverData) {
    const driversGrid = document.querySelector('.drivers-grid');

    if (!driversGrid) return;

<<<<<<< HEAD
    // Create new driver card
    const driverCard = document.createElement('div');
    driverCard.className = 'driver-card';
    driverCard.setAttribute('data-driver-id', driverData.id);
    driverCard.setAttribute('data-driver-name', driverData.fullName || driverData.name || 'N/A');
    driverCard.setAttribute('data-driver-phone', driverData.contactNumber || driverData.phone || 'N/A');

    driverCard.innerHTML = `
        <div class="driver-avatar">
            ${driverData.profileImageUrl ?
            `<img src="${driverData.profileImageUrl}" alt="${driverData.fullName || driverData.name}" style="width: 60px; height: 60px; border-radius: 50%; object-fit: cover;">` :
            `<i class="fas fa-user-circle"></i>`
        }
        </div>
        <h4>${driverData.fullName || driverData.name || 'N/A'}</h4>
        <p class="driver-vehicle">${driverData.address || driverData.vehicle || 'No address'}</p>
=======
    //Create new driver card
    const driverCard = document.createElement('div');
    driverCard.className = 'driver-card';
    driverCard.setAttribute('data-driver-name', driverData.name);
    driverCard.setAttribute('data-driver-phone', driverData.phone || 'N/A');
    driverCard.setAttribute('data-driver-license', driverData.license || 'N/A');

    driverCard.innerHTML = `
        <div class="driver-avatar">
            <i class="fas fa-user-circle"></i>
        </div>
        <h4>${driverData.name}</h4>
        <p class="driver-vehicle">${driverData.vehicle}</p>
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
        <p class="driver-status active">Active</p>
        <button class="btn-view-details">View Details</button>
    `;

    // Add click event to new button
    const viewBtn = driverCard.querySelector('.btn-view-details');
<<<<<<< HEAD
    viewBtn.addEventListener('click', () => {
        handleViewDetails({ target: viewBtn });
    });
=======
    viewBtn.addEventListener('click', handleViewDetails);
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))

    // Add with animation
    driverCard.style.opacity = '0';
    driverCard.style.transform = 'scale(0.8)';
    driversGrid.appendChild(driverCard);

    // Animate in
    setTimeout(() => {
        driverCard.style.transition = 'all 0.3s ease-out';
        driverCard.style.opacity = '1';
        driverCard.style.transform = 'scale(1)';
    }, 50);
}

<<<<<<< HEAD

// Reverse geocoding: Convert coordinates to location name
async function getLocationName(lat, lng) {
    try {
        // Use OpenStreetMap Nominatim API for reverse geocoding (free, no API key required)
        const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=16&addressdetails=1`,
            {
                headers: {
                    'User-Agent': 'GeoKids Admin Dashboard' // Required by Nominatim
                }
            }
        );

        if (!response.ok) {
            throw new Error('Geocoding request failed');
        }

        const data = await response.json();

        // Build a readable address from the response
        const address = data.address;
        let locationParts = [];

        // Priority: road/suburb/city/state
        if (address.road) locationParts.push(address.road);
        if (address.suburb || address.neighbourhood) {
            locationParts.push(address.suburb || address.neighbourhood);
        }
        if (address.city || address.town || address.village) {
            locationParts.push(address.city || address.town || address.village);
        }

        // If we have parts, join them
        if (locationParts.length > 0) {
            return locationParts.join(', ');
        }

        // Fallback to display_name if structured data not available
        if (data.display_name) {
            // Take first 2-3 parts of display name for brevity
            const parts = data.display_name.split(', ');
            return parts.slice(0, 3).join(', ');
        }

        // Ultimate fallback
        return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;

    } catch (error) {
        console.error('Error in reverse geocoding:', error);
        // Return coordinates as fallback
        return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    }
}

// Show comprehensive driver details modal with Firebase data
async function showDriverDetailsModal(driverData) {
=======
// Show comprehensive driver details modal
function showDriverDetailsModal(driverData) {
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
    // Remove existing modal if any
    const existingModal = document.getElementById('driver-details-modal');
    if (existingModal) {
        existingModal.remove();
    }

<<<<<<< HEAD
    // Show loading modal first
    showLoadingModal();

    try {
        // Fetch full driver details from Firebase if we have an ID
        let driverDetails;
        if (driverData.id) {
            driverDetails = await window.FirebaseService.getDriverById(driverData.id);
        } else {
            // If no ID, try to find by name (fallback for old implementation)
            const allDrivers = await window.FirebaseService.getActiveDrivers();
            driverDetails = allDrivers.find(d => d.fullName === driverData.name);

            if (!driverDetails) {
                throw new Error('Driver not found');
            }
        }

        // Remove loading modal
        removeLoadingModal();

        // Extract route data
        const routeData = driverDetails.routeData || {};
        const distance = routeData.distance || 'N/A';
        const duration = routeData.duration || 'N/A';
        const startPoint = routeData.startPoint || { lat: 0, lng: 0 };
        const endPoint = routeData.endPoint || { lat: 0, lng: 0 };
        const pathCoordinates = routeData.pathCoordinates || [];

        // Get location names for start and end points
        let startLocationName = 'Loading...';
        let endLocationName = 'Loading...';

        // We'll update these after the modal is displayed
        const locationPromises = Promise.all([
            getLocationName(startPoint.lat, startPoint.lng),
            getLocationName(endPoint.lat, endPoint.lng)
        ]);

        // Create comprehensive modal
        const modal = document.createElement('div');
        modal.id = 'driver-details-modal';
        modal.className = 'driver-modal-overlay';

        modal.innerHTML = `
            <div class="modal-content-large">
                <!-- Orange Header -->
                <div class="modal-header-orange">
                    <h2><i class="fa fa-user-circle"></i> Driver Profile</h2>
                    <button id="close-modal" class="modal-close-btn">&times;</button>
                </div>
                
                <div class="modal-body-scroll">
                    <!-- Profile Section -->
                    <div class="driver-profile-section">
                        <div class="profile-image-section">
                            <img src="${driverDetails.profileImageUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(driverDetails.fullName || 'Driver')}&size=200&background=FF6B3D&color=fff&bold=true`}" 
                                 alt="Profile" class="driver-profile-img" 
                                 onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(driverDetails.fullName || 'Driver')}&size=200&background=FF6B3D&color=fff&bold=true'">
                        </div>
                        
                        <div class="profile-info-section">
                            <h3>${driverDetails.fullName || 'N/A'}</h3>
                            
                            <div class="contact-grid">
                                <div class="contact-item">
                                    <p class="label"><i class="fas fa-phone"></i> Phone</p>
                                    <p class="value">${driverDetails.contactNumber || 'N/A'}</p>
                                </div>
                                <div class="contact-item">
                                    <p class="label"><i class="fas fa-id-card"></i> NIC</p>
                                    <p class="value">${driverDetails.nic || 'N/A'}</p>
                                </div>
                                <div class="contact-item">
                                    <p class="label"><i class="fas fa-map-marker-alt"></i> Address</p>
                                    <p class="value">${driverDetails.address || 'N/A'}</p>
                                </div>
                                <div class="contact-item">
                                    <p class="label"><i class="fas fa-birthday-cake"></i> Birthday</p>
                                    <p class="value">${driverDetails.birthday || 'N/A'}</p>
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
                                <img src="${driverDetails.frontLicenseUrl || 'https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Front+License'}" 
                                     alt="Front License" 
                                     class="license-img clickable-img"
                                     onclick="window.open(this.src, '_blank')"
                                     onerror="this.src='https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Front+License'">
                            </div>
                            <div class="license-item">
                                <h5>Back Side</h5>
                                <img src="${driverDetails.backLicenseUrl || 'https://via.placeholder.com/400x250/E5E7EB/6B7280?text=No+Back+License'}" 
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
                            <div class="coordinate-item" id="start-location">
                                <strong>Start:</strong> <span class="location-name">Loading...</span>
                                <span class="location-coords">${startPoint.lat.toFixed(6)}, ${startPoint.lng.toFixed(6)}</span>
                            </div>
                            <div class="coordinate-item" id="end-location">
                                <strong>End:</strong> <span class="location-name">Loading...</span>
                                <span class="location-coords">${endPoint.lat.toFixed(6)}, ${endPoint.lng.toFixed(6)}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Vehicle Images Section -->
                    ${(driverDetails.vehicleImageUrls && driverDetails.vehicleImageUrls.length > 0) ? `
                        <div class="vehicle-section">
                            <h4><i class="fas fa-car"></i> Vehicle Images</h4>
                            <div class="vehicle-carousel">
                                ${driverDetails.vehicleImageUrls.map((url, index) => `
                                    <div class="vehicle-carousel-item ${index === 0 ? 'active' : ''}" data-index="${index}">
                                        <img src="${url}" 
                                             alt="Vehicle ${index + 1}" 
                                             class="vehicle-carousel-img clickable-img"
                                             onclick="window.open(this.src, '_blank')"
                                             onerror="this.src='https://via.placeholder.com/600x400/E5E7EB/6B7280?text=Vehicle+Image'">
                                    </div>
                                `).join('')}
                            </div>
                            ${driverDetails.vehicleImageUrls.length > 1 ? `
                                <div class="carousel-controls">
                                    <button class="carousel-btn prev-btn" onclick="changeVehicleImage(-1)">
                                        <i class="fas fa-chevron-left"></i>
                                    </button>
                                    <div class="carousel-dots">
                                        ${driverDetails.vehicleImageUrls.map((_, index) => `
                                            <span class="dot ${index === 0 ? 'active' : ''}" onclick="setVehicleImage(${index})"></span>
                                        `).join('')}
                                    </div>
                                    <button class="carousel-btn next-btn" onclick="changeVehicleImage(1)">
                                        <i class="fas fa-chevron-right"></i>
                                    </button>
                                </div>
                            ` : ''}
                        </div>
                    ` : ''}
                    
                    <!-- Action Buttons -->
                    <div class="modal-actions">
                        <button class="btn-modal btn-close-modal">
                            <i class="fas fa-times"></i> Close
                        </button>
                        <button class="btn-modal btn-contact">
                            <i class="fas fa-phone"></i> Contact Driver
                        </button>
                    </div>
                </div>
            </div>
        `;

        // Add styles
        addModalStyles();

        document.body.appendChild(modal);

        // Event handlers
        const closeModal = () => {
            modal.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => modal.remove(), 300);
        };

        modal.querySelector('#close-modal').addEventListener('click', closeModal);
        modal.querySelector('.btn-close-modal').addEventListener('click', closeModal);
        modal.querySelector('.btn-contact').addEventListener('click', () => {
            window.location.href = `tel:${driverDetails.contactNumber}`;
        });
        modal.addEventListener('click', (e) => {
            if (e.target.classList.contains('driver-modal-overlay')) closeModal();
        });

        // Fetch and update location names asynchronously
        locationPromises.then(([startName, endName]) => {
            const startEl = document.getElementById('start-location');
            const endEl = document.getElementById('end-location');

            if (startEl) {
                const startLocationSpan = startEl.querySelector('.location-name');
                if (startLocationSpan) {
                    startLocationSpan.textContent = startName;
                }
            }

            if (endEl) {
                const endLocationSpan = endEl.querySelector('.location-name');
                if (endLocationSpan) {
                    endLocationSpan.textContent = endName;
                }
            }
        }).catch(error => {
            console.error('Error fetching location names:', error);
            // Keep showing coordinates if geocoding fails
            const startEl = document.getElementById('start-location');
            const endEl = document.getElementById('end-location');

            if (startEl) {
                const startLocationSpan = startEl.querySelector('.location-name');
                if (startLocationSpan) {
                    startLocationSpan.textContent = 'Location unavailable';
                }
            }

            if (endEl) {
                const endLocationSpan = endEl.querySelector('.location-name');
                if (endLocationSpan) {
                    endLocationSpan.textContent = 'Location unavailable';
                }
            }
        });

    } catch (error) {
        removeLoadingModal();
        console.error('Error loading driver details:', error);
        showErrorModal(error.message || 'Failed to load driver details');
    }
}

// Helper: Show loading modal
function showLoadingModal() {
    const loadingModal = document.createElement('div');
    loadingModal.id = 'loading-modal';
    loadingModal.className = 'driver-modal-overlay';
    loadingModal.innerHTML = `
        <div class="loading-content">
            <div class="spinner"></div>
            <p>Loading driver details...</p>
        </div>
    `;
    document.body.appendChild(loadingModal);
}

// Helper: Remove loading modal
function removeLoadingModal() {
    const loadingModal = document.getElementById('loading-modal');
    if (loadingModal) {
        loadingModal.remove();
    }
}

// Helper: Show error modal
function showErrorModal(message) {
    const errorModal = document.createElement('div');
    errorModal.id = 'error-modal';
    errorModal.className = 'driver-modal-overlay';
    errorModal.innerHTML = `
        <div class="error-content">
            <i class="fas fa-exclamation-circle"></i>
            <h3>Error</h3>
            <p>${message}</p>
            <button class="btn-modal btn-close-modal">Close</button>
        </div>
    `;
    document.body.appendChild(errorModal);

    errorModal.querySelector('.btn-close-modal').addEventListener('click', () => {
        errorModal.remove();
    });
}

// Vehicle carousel navigation
window.changeVehicleImage = function (direction) {
    const items = document.querySelectorAll('.vehicle-carousel-item');
    const dots = document.querySelectorAll('.carousel-dots .dot');
    let currentIndex = 0;

    items.forEach((item, index) => {
        if (item.classList.contains('active')) {
            currentIndex = index;
        }
        item.classList.remove('active');
    });

    currentIndex = (currentIndex + direction + items.length) % items.length;

    items[currentIndex].classList.add('active');
    dots.forEach((dot, index) => {
        dot.classList.toggle('active', index === currentIndex);
    });
};

window.setVehicleImage = function (index) {
    const items = document.querySelectorAll('.vehicle-carousel-item');
    const dots = document.querySelectorAll('.carousel-dots .dot');

    items.forEach((item, i) => {
        item.classList.toggle('active', i === index);
    });
    dots.forEach((dot, i) => {
        dot.classList.toggle('active', i === index);
    });
};

=======
    // Get additional data from card attributes if available
    const card = event.target.closest('.driver-card');
    if (card) {
        driverData.phone = card.getAttribute('data-driver-phone') || 'N/A';
        driverData.license = card.getAttribute('data-driver-license') || 'N/A';
    }

    // Mock comprehensive data - replace with Firebase data in production
    const driverDetails = {
        profileImage: `https://ui-avatars.com/api/?name=${encodeURIComponent(driverData.name || 'Driver')}&size=200&background=FF6B3D&color=fff&bold=true`,
        email: 'driver@geokids.com',
        address: '123 Main Street, Colombo',
        experience: '5 years',
        rating: '4.8',
        vehicleModel: driverData.vehicle || 'Toyota Hiace',
        vehicleNumber: 'ABC-1234',
        vehicleColor: 'White',
        vehicleCapacity: '12 seats',
        vehicleImage: 'https://via.placeholder.com/400x250/FF6B3D/FFFFFF?text=Vehicle+Image',
        assignedStudents: [
            { name: 'Emma Wilson', grade: 'Grade 5', pickup: '7:30 AM' },
            { name: 'Noah Brown', grade: 'Grade 3', pickup: '7:45 AM' },
            { name: 'Olivia Davis', grade: 'Grade 4', pickup: '8:00 AM' }
        ]
    };

    // Create comprehensive modal
    const modal = document.createElement('div');
    modal.id = 'driver-details-modal';
    modal.className = 'driver-modal-overlay';

    modal.innerHTML = `
        <div class="modal-content-large">
            <!-- Orange Header -->
            <div class="modal-header-orange">
                <h2><i class="fa fa-user-circle"></i> Driver Profile</h2>
                <button id="close-modal" class="modal-close-btn">&times;</button>
            </div>
            
            <div class="modal-body-scroll">
                <!-- Profile Section -->
                <div class="driver-profile-section">
                    <div class="profile-image-section">
                        <img src="${driverDetails.profileImage}" alt="Profile" class="driver-profile-img">
                        <div class="rating-badge">
                            <i class="fas fa-star"></i> ${driverDetails.rating}/5.0
                        </div>
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${driverData.name}</h3>
                        <p class="experience-text"><i class="fas fa-briefcase"></i> ${driverDetails.experience} experience</p>
                        
                        <div class="contact-grid">
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-phone"></i> Phone</p>
                                <p class="value">${driverData.phone || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-envelope"></i> Email</p>
                                <p class="value">${driverDetails.email}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-id-card"></i> License</p>
                                <p class="value">${driverData.license || 'N/A'}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Vehicle Section -->
                <div class="vehicle-section">
                    <h4><i class="fas fa-car"></i> Vehicle Information</h4>
                    
                    <div class="vehicle-grid">
                        <div class="vehicle-image-col">
                            <img src="${driverDetails.vehicleImage}" alt="Vehicle" class="vehicle-img">
                            <div class="vehicle-specs">
                                <div class="spec-item">
                                    <span class="spec-label">Model</span>
                                    <span class="spec-value">${driverDetails.vehicleModel}</span>
                                </div>
                                <div class="spec-item">
                                    <span class="spec-label">Number</span>
                                    <span class="spec-value">${driverDetails.vehicleNumber}</span>
                                </div>
                                <div class="spec-item">
                                    <span class="spec-label">Color</span>
                                    <span class="spec-value">${driverDetails.vehicleColor}</span>
                                </div>
                                <div class="spec-item">
                                    <span class="spec-label">Capacity</span>
                                    <span class="spec-value">${driverDetails.vehicleCapacity}</span>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Students List -->
                        <div class="students-col">
                            <h5><i class="fas fa-users"></i> Assigned Students (${driverDetails.assignedStudents.length})</h5>
                            <div class="students-list">
                                ${driverDetails.assignedStudents.map(student => `
                                    <div class="student-item">
                                        <div class="student-info">
                                            <p class="student-name">${student.name}</p>
                                            <p class="student-grade">${student.grade}</p>
                                        </div>
                                        <div class="pickup-time">
                                            <p class="time-label">Pickup</p>
                                            <p class="time-value">${student.pickup}</p>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Action Buttons -->
                <div class="modal-actions">
                    <button class="btn-modal btn-close-modal">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-contact">
                        <i class="fas fa-phone"></i> Contact Driver
                    </button>
                    <button class="btn-modal btn-route">
                        <i class="fas fa-route"></i> View Route
                    </button>
                </div>
            </div>
        </div>
    `;

    // Add styles
    addModalStyles();

    document.body.appendChild(modal);

    // Event handlers
    const closeModal = () => {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    };

    modal.querySelector('#close-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-close-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-contact').addEventListener('click', () => {
        window.location.href = `tel:${driverData.phone}`;
    });
    modal.querySelector('.btn-route').addEventListener('click', () => {
        alert('Route view feature coming soon!');
    });
    modal.addEventListener('click', (e) => {
        if (e.target.classList.contains('driver-modal-overlay')) closeModal();
    });
}

>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
// Add modal styles dynamically
function addModalStyles() {
    if (document.getElementById('driver-modal-styles')) return;

    const style = document.createElement('style');
    style.id = 'driver-modal-styles';
    style.textContent = `
        .driver-modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.6);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            animation: fadeIn 0.3s ease;
            padding: 1rem;
            overflow-y: auto;
        }
        
        .modal-content-large {
            background: white;
            border-radius: 1.5rem;
            max-width: 900px;
            width: 100%;
            max-height: 90vh;
            overflow: hidden;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.3);
            animation: slideUp 0.3s ease;
            display: flex;
            flex-direction: column;
        }
        
        .modal-header-orange {
            background: linear-gradient(135deg, #FF6B3D 0%, #E85A2F 100%);
            color: white;
            padding: 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header-orange h2 {
            font-size: 1.75rem;
            font-weight: 800;
            margin: 0;
        }
        
        .modal-close-btn {
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            font-size: 2rem;
            width: 40px;
            height: 40px;
            border-radius: 0.5rem;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .modal-close-btn:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: rotate(90deg);
        }
        
        .modal-body-scroll {
            padding: 2rem;
            overflow-y: auto;
            flex: 1;
        }
        
        .driver-profile-section {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: 2rem;
            margin-bottom: 2rem;
            align-items: start;
        }
        
        .profile-image-section {
            text-align: center;
        }
        
        .driver-profile-img {
            width: 150px;
            height: 150px;
            border-radius: 50%;
            border: 5px solid #FF6B3D;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }
        
        .rating-badge {
            margin-top: 1rem;
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
            padding: 0.5rem 1rem;
            border-radius: 2rem;
            font-size: 0.875rem;
            font-weight: 600;
        }
        
        .profile-info-section h3 {
            font-size: 1.5rem;
            font-weight: 800;
            color: #1E293B;
            margin: 0 0 0.5rem 0;
        }
        
        .experience-text {
            color: #64748B;
            margin: 0 0 1rem 0;
        }
        
        .contact-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
        }
        
        .contact-item {
            background: #F7F9FC;
            padding: 1rem;
            border-radius: 0.75rem;
        }
        
        .contact-item .label {
            font-size: 0.875rem;
            color: #647B;
            margin: 0 0 0.25rem 0;
        }
        
        .contact-item .value {
            font-weight: 600;
            color: #1E293B;
            margin: 0;
        }
        
        .vehicle-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .vehicle-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .vehicle-section h4 i {
            color: #FF6B3D;
        }
        
        .vehicle-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.5rem;
        }
        
        .vehicle-img {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 0.75rem;
            border: 2px solid #CBD5E1;
            margin-bottom: 1rem;
        }
        
        .vehicle-specs {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 0.75rem;
        }
        
        .spec-item {
            display: flex;
            flex-direction: column;
        }
        
        .spec-label {
            font-size: 0.75rem;
            color: #64748B;
        }
        
        .spec-value {
            font-weight: 600;
            color: #1E293B;
        }
        
        .students-col h5 {
            font-size: 1rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .students-col h5 i {
            color: #FF6B3D;
        }
        
        .students-list {
            max-height: 240px;
            overflow-y: auto;
        }
        
        .student-item {
            background: white;
            padding: 0.75rem;
            border-radius: 0.5rem;
            margin-bottom: 0.5rem;
            border-left: 3px solid #FF6B3D;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .student-name {
            font-weight: 600;
            color: #1E293B;
            margin: 0;
            font-size: 0.9rem;
        }
        
        .student-grade {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
        }
        
        .pickup-time {
            text-align: right;
        }
        
        .time-label {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
        }
        
        .time-value {
            font-weight: 600;
            color: #FF6B3D;
            margin: 0;
            font-size: 0.875rem;
        }
        
        .modal-actions {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
        }
        
        .btn-modal {
            flex: 1;
            padding: 1rem;
            border: none;
            border-radius: 0.75rem;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .btn-close-modal {
            background: #64748B;
            color: white;
        }
        
        .btn-close-modal:hover {
            background: #475569;
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
        }
        
        .btn-contact {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
        }
        
        .btn-contact:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
        }
        
        .btn-route {
            background: linear-gradient(135deg, #FF6B3D 0%, #E85A2F 100%);
            color: white;
        }
        
        .btn-route:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(255, 107, 61, 0.4);
        }
        
<<<<<<< HEAD
        /* License Section Styles */
        .license-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .license-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .license-section h4 i {
            color: #FF6B3D;
        }
        
        .license-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.5rem;
        }
        
        .license-item h5 {
            font-size: 0.875rem;
            font-weight: 600;
            color: #64748B;
            margin: 0 0 0.5rem 0;
        }
        
        .license-img {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 0.75rem;
            border: 2px solid #CBD5E1;
            transition: all 0.3s ease;
        }
        
        .clickable-img {
            cursor: pointer;
        }
        
        .clickable-img:hover {
            transform: scale(1.02);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }
        
        /* Route Section Styles */
        .route-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .route-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .route-section h4 i {
            color: #FF6B3D;
        }
        
        .route-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1rem;
            margin-bottom: 1rem;
        }
        
        .route-stat-item {
            background: white;
            padding: 1rem;
            border-radius: 0.75rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        
        .route-stat-item i {
            font-size: 1.5rem;
            color: #FF6B3D;
        }
        
        .stat-label {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
        }
        
        .stat-value {
            font-size: 1rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0;
        }
        
        .route-coordinates {
            background: white;
            padding: 1rem;
            border-radius: 0.75rem;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }
        
        .coordinate-item {
            font-size: 0.875rem;
            color: #64748B;
            display: flex;
            flex-direction: column;
            gap: 0.25rem;
        }
        
        .coordinate-item strong {
            color: #1E293B;
            margin-right: 0.5rem;
        }
        
        .location-name {
            font-size: 1rem;
            font-weight: 600;
            color: #1E293B;
            display: block;
        }
        
        .location-coords {
            font-size: 0.75rem;
            color: #94A3B8;
            display: block;
            margin-top: 0.25rem;
        }
        
        /* Vehicle Carousel Styles */
        .vehicle-carousel {
            position: relative;
            width: 100%;
            margin-bottom: 1rem;
        }
        
        .vehicle-carousel-item {
            display: none;
        }
        
        .vehicle-carousel-item.active {
            display: block;
        }
        
        .vehicle-carousel-img {
            width: 100%;
            height: 300px;
            object-fit: cover;
            border-radius: 0.75rem;
            border: 2px solid #CBD5E1;
        }
        
        .carousel-controls {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 1rem;
            margin-top: 1rem;
        }
        
        .carousel-btn {
            background: #FF6B3D;
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .carousel-btn:hover {
            background: #E85A2F;
            transform: scale(1.1);
        }
        
        .carousel-dots {
            display: flex;
            gap: 0.5rem;
        }
        
        .dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #CBD5E1;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .dot.active {
            background: #FF6B3D;
            transform: scale(1.2);
        }
        
        /* Loading Modal Styles */
        .loading-content {
            background: white;
            padding: 3rem;
            border-radius: 1rem;
            text-align: center;
        }
        
        .spinner {
            width: 50px;
            height: 50px;
            margin: 0 auto 1rem;
            border: 4px solid #F3F4F6;
            border-top-color: #FF6B3D;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        
        .loading-content p {
            color: #64748B;
            font-weight: 600;
        }
        
        /* Error Modal Styles */
        .error-content {
            background: white;
            padding: 2.5rem;
            border-radius: 1rem;
            text-align: center;
            max-width: 400px;
        }
        
        .error-content i {
            font-size: 3rem;
            color: #EF4444;
            margin-bottom: 1rem;
        }
        
        .error-content h3 {
            font-size: 1.5rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .error-content p {
            color: #64748B;
            margin: 0 0 1.5rem 0;
        }
        
=======
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        
        @keyframes slideUp {
            from {
                transform: translateY(30px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }
        
        @keyframes fadeOut {
            to { opacity: 0; }
        }
        
        @media (max-width: 768px) {
            .driver-profile-section {
                grid-template-columns: 1fr;
                text-align: center;
            }
            
            .vehicle-grid {
                grid-template-columns: 1fr;
            }
            
<<<<<<< HEAD
            .license-grid {
                grid-template-columns: 1fr;
            }
            
            .route-stats {
                grid-template-columns: 1fr;
            }
            
=======
>>>>>>> 327c339 (Implement Driver Application Status Management and Conditional Navigation(refs #16))
            .modal-actions {
                flex-direction: column;
            }
        }
    `;
    document.head.appendChild(style);
}

// Export functions
window.DriverManagement = {
    addDriverToActiveList,
    showDriverDetailsModal
};
