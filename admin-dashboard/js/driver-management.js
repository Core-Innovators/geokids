
// Add approved driver to active drivers list
function addDriverToActiveList(driverData) {
    const driversGrid = document.querySelector('.drivers-grid');

    if (!driversGrid) return;

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
        <p class="driver-status active">Active</p>
        <button class="btn-view-details">View Details</button>
    `;

    // Add click event to new button
    const viewBtn = driverCard.querySelector('.btn-view-details');
    viewBtn.addEventListener('click', handleViewDetails);

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

// Show comprehensive driver details modal
function showDriverDetailsModal(driverData) {
    // Remove existing modal if any
    const existingModal = document.getElementById('driver-details-modal');
    if (existingModal) {
        existingModal.remove();
    }

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
