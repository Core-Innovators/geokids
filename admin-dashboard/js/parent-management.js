// ===================================
// Parent Management Helper Functions
// ===================================

// Show parent and children details modal
function showParentDetailsModal(parentData) {
    // Remove existing modal if any
    const existingModal = document.getElementById('parent-details-modal');
    if (existingModal) {
        existingModal.remove();
    }

    // Mock data for children - in production this would come from Firebase
    const mockChildren = parentData.children || [
        {
            name: 'Sarah ' + parentData.name.split(' ')[1],
            age: 8,
            grade: 'Grade 3',
            school: 'St. Mary\'s School',
            pickupTime: '7:30 AM',
            dropoffTime: '2:00 PM'
        },
        {
            name: 'Tom ' + parentData.name.split(' ')[1],
            age: 6,
            grade: 'Grade 1',
            school: 'St. Mary\'s School',
            pickupTime: '7:30 AM',
            dropoffTime: '2:00 PM'
        }
    ];

    const profileImage = `https://ui-avatars.com/api/?name=${encodeURIComponent(parentData.name)}&size=200&background=3B82F6&color=fff&bold=true`;

    const modal = document.createElement('div');
    modal.id = 'parent-details-modal';
    modal.className = 'parent-modal-overlay';

    modal.innerHTML = `
        <div class="modal-content-large">
            <!-- Blue Header for Parents -->
            <div class="modal-header-blue">
                <h2><i class="fas fa-users"></i> Parent & Children Details</h2>
                <button id="close-parent-modal" class="modal-close-btn">&times;</button>
            </div>
            
            <div class="modal-body-scroll">
                <!-- Parent Profile Section -->
                <div class="parent-profile-section">
                    <div class="profile-image-section">
                        <img src="${profileImage}" alt="Profile" class="parent-profile-img">
                        <div class="status-badge-parent">
                            <i class="fas fa-check-circle"></i> Active
                        </div>
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${parentData.name}</h3>
                        <p class="parent-subtitle"><i class="fas fa-user-friends"></i> Guardian</p>
                        
                        <div class="contact-grid">
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-phone"></i> Phone</p>
                                <p class="value">${parentData.phone}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-envelope"></i> Email</p>
                                <p class="value">${parentData.email}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-calendar"></i> Joined</p>
                                <p class="value">${parentData.joined || 'Jan 2025'}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Children Section -->
                <div class="children-section">
                    <h4><i class="fas fa-child"></i> Registered Children (${mockChildren.length})</h4>
                    
                    <div class="children-grid">
                        ${mockChildren.map(child => `
                            <div class="child-card">
                                <div class="child-header">
                                    <div class="child-avatar">
                                        <i class="fas fa-child"></i>
                                    </div>
                                    <div class="child-basic-info">
                                        <h5>${child.name}</h5>
                                        <p class="child-meta">${child.age} years old • ${child.grade}</p>
                                    </div>
                                </div>
                                
                                <div class="child-details">
                                    <div class="child-detail-item">
                                        <i class="fas fa-school"></i>
                                        <span>${child.school}</span>
                                    </div>
                                    <div class="child-detail-item">
                                        <i class="fas fa-clock"></i>
                                        <span>Pickup: ${child.pickupTime}</span>
                                    </div>
                                    <div class="child-detail-item">
                                        <i class="fas fa-clock"></i>
                                        <span>Drop-off: ${child.dropoffTime}</span>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
                
                <!-- Trip History Summary -->
                <div class="trip-summary-section">
                    <h4><i class="fas fa-route"></i> Trip Summary</h4>
                    <div class="summary-stats">
                        <div class="stat-box">
                            <div class="stat-icon"><i class="fas fa-calendar-check"></i></div>
                            <div class="stat-info">
                                <p class="stat-value">24</p>
                                <p class="stat-label">Total Trips</p>
                            </div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
                            <div class="stat-info">
                                <p class="stat-value">23</p>
                                <p class="stat-label">Completed</p>
                            </div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-icon"><i class="fas fa-clock"></i></div>
                            <div class="stat-info">
                                <p class="stat-value">95%</p>
                                <p class="stat-label">On-Time Rate</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Action Buttons -->
                <div class="modal-actions">
                    <button class="btn-modal btn-close-parent">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-message">
                        <i class="fas fa-envelope"></i> Send Message
                    </button>
                    <button class="btn-modal btn-view-trips">
                        <i class="fas fa-route"></i> View Trips
                    </button>
                </div>
            </div>
        </div>
    `;

    // Add styles
    addParentModalStyles();

    document.body.appendChild(modal);

    // Event handlers
    const closeModal = () => {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    };

    modal.querySelector('#close-parent-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-close-parent').addEventListener('click', closeModal);
    modal.querySelector('.btn-message').addEventListener('click', () => {
        alert(`Send message to ${parentData.name} at ${parentData.email}`);
    });
    modal.querySelector('.btn-view-trips').addEventListener('click', () => {
        alert('Trip history view coming soon!');
    });
    modal.addEventListener('click', (e) => {
        if (e.target.classList.contains('parent-modal-overlay')) closeModal();
    });
}

// Add parent modal styles
function addParentModalStyles() {
    if (document.getElementById('parent-modal-styles')) return;

    const style = document.createElement('style');
    style.id = 'parent-modal-styles';
    style.textContent = `
        .parent-modal-overlay {
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
        
        .modal-header-blue {
            background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
            color: white;
            padding: 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header-blue h2 {
            font-size: 1.75rem;
            font-weight: 800;
            margin: 0;
        }
        
        .parent-profile-section {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: 2rem;
            margin-bottom: 2rem;
            align-items: start;
        }
        
        .parent-profile-img {
            width: 150px;
            height: 150px;
            border-radius: 50%;
            border: 5px solid #3B82F6;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }
        
        .status-badge-parent {
            margin-top: 1rem;
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
            padding: 0.5rem 1rem;
            border-radius: 2rem;
            font-size: 0.875rem;
            font-weight: 600;
        }
        
        .parent-subtitle {
            color: #64748B;
            margin: 0 0 1rem 0;
        }
        
        .children-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .children-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .children-section h4 i {
            color: #3B82F6;
        }
        
        .children-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1rem;
        }
        
        .child-card {
            background: white;
            border-radius: 0.75rem;
            padding: 1.25rem;
            border-left: 4px solid #3B82F6;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .child-header {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
            align-items: center;
        }
        
        .child-avatar {
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.5rem;
        }
        
        .child-basic-info h5 {
            font-size: 1.125rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 0.25rem 0;
        }
        
        .child-meta {
            font-size: 0.875rem;
            color: #64748B;
            margin: 0;
        }
        
        .child-details {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }
        
        .child-detail-item {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            color: #475569;
        }
        
        .child-detail-item i {
            color: #3B82F6;
            width: 16px;
        }
        
        .trip-summary-section {
            background: white;
            padding: 1.5rem;
            border-radius: 1rem;
            border: 2px solid #E5E9F2;
            margin-bottom: 2rem;
        }
        
        .trip-summary-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .trip-summary-section h4 i {
            color: #3B82F6;
        }
        
        .summary-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
        }
        
        .stat-box {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1rem;
            background: #F7F9FC;
            border-radius: 0.75rem;
        }
        
        .stat-icon {
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
            border-radius: 0.75rem;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.25rem;
        }
        
        .stat-value {
            font-size: 1.5rem;
            font-weight: 800;
            color: #1E293B;
            margin: 0;
        }
        
        .stat-label {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
        }
        
        .btn-message {
            background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
            color: white;
        }
        
        .btn-message:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(139, 92, 246, 0.4);
        }
        
        .btn-view-trips {
            background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
            color: white;
        }
        
        .btn-view-trips:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.4);
        }
        
        @media (max-width: 768px) {
            .parent-profile-section {
                grid-template-columns: 1fr;
                text-align: center;
            }
            
            .children-grid {
                grid-template-columns: 1fr;
            }
        }
    `;
    document.head.appendChild(style);
}

// Show child and parent details modal from Firebase data
function showChildDetailsModal(childData) {
    // Remove existing modal if any
    const existingModal = document.getElementById('child-details-modal');
    if (existingModal) {
        existingModal.remove();
    }

    // Create profile image URL
    const profileImage = `https://ui-avatars.com/api/?name=${encodeURIComponent(childData.childName || 'Child')}&size=200&background=EC4899&color=fff&bold=true`;

    // Format dates
    const createdDate = childData.createdAt ? new Date(childData.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    }) : 'N/A';

    const updatedDate = childData.updatedAt ? new Date(childData.updatedAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    }) : 'N/A';

    // Status badge color
    const statusColor = childData.status === 'active' ? '#10B981' : '#F59E0B';
    const statusText = childData.status === 'active' ? 'Active' : 'Pending';

    const modal = document.createElement('div');
    modal.id = 'child-details-modal';
    modal.className = 'parent-modal-overlay';

    modal.innerHTML = `
        <div class="modal-content-large">
            <!-- Header -->
            <div class="modal-header-child">
                <h2><i class="fas fa-child"></i> Child & Parent Details</h2>
                <button id="close-child-modal" class="modal-close-btn">&times;</button>
            </div>
            
            <div class="modal-body-scroll">
                <!-- Child Profile Section -->
                <div class="child-profile-section">
                    <div class="profile-image-section">
                        <img src="${profileImage}" alt="Profile" class="child-profile-img">
                        <div class="status-badge-child" style="background: linear-gradient(135deg, ${statusColor} 0%, ${statusColor}dd 100%);">
                            <i class="fas fa-${childData.status === 'active' ? 'check-circle' : 'clock'}"></i> ${statusText}
                        </div>
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${childData.childName || 'N/A'}</h3>
                        <p class="child-subtitle"><i class="fas fa-graduation-cap"></i> Grade ${childData.childGrade || 'N/A'} Student</p>
                        
                        <div class="info-grid">
                            <div class="info-item">
                                <p class="label"><i class="fas fa-birthday-cake"></i> Age</p>
                                <p class="value">${childData.childAge || 'N/A'} years old</p>
                            </div>
                            <div class="info-item">
                                <p class="label"><i class="fas fa-school"></i> School</p>
                                <p class="value">${childData.childSchool || 'N/A'}</p>
                            </div>
                            <div class="info-item">
                                <p class="label"><i class="fas fa-calendar-alt"></i> Registered</p>
                                <p class="value">${createdDate}</p>
                            </div>
                            <div class="info-item">
                                <p class="label"><i class="fas fa-sync-alt"></i> Last Updated</p>
                                <p class="value">${updatedDate}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Parent/Guardian Section -->
                <div class="parent-guardian-section">
                    <h4><i class="fas fa-user-friends"></i> Parent / Guardian Information</h4>
                    
                    <div class="guardian-card">
                        <div class="guardian-avatar">
                            <i class="fas fa-user"></i>
                        </div>
                        <div class="guardian-details">
                            <div class="guardian-header">
                                <h5>${childData.parentName || 'N/A'}</h5>
                                <span class="guardian-badge">Primary Guardian</span>
                            </div>
                            
                            <div class="guardian-contacts">
                                <div class="contact-row">
                                    <i class="fas fa-id-card"></i>
                                    <span><strong>NIC:</strong> ${childData.parentNic || 'N/A'}</span>
                                </div>
                                <div class="contact-row">
                                    <i class="fas fa-phone"></i>
                                    <span><strong>Primary Contact:</strong> ${childData.parentContact1 || 'N/A'}</span>
                                </div>
                                <div class="contact-row">
                                    <i class="fas fa-phone-alt"></i>
                                    <span><strong>Secondary Contact:</strong> ${childData.parentContact2 || 'N/A'}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Driver Assignment Section -->
                <div class="driver-assignment-section">
                    <h4><i class="fas fa-bus"></i> Transport Assignment</h4>
                    
                    <div class="assignment-status in-progress">
                        <div class="status-icon">
                            <i class="fas fa-hourglass-half"></i>
                        </div>
                        <div class="status-info">
                            <h5>Driver Assignment In Progress</h5>
                            <p>No driver has been assigned to this child yet. The transport arrangement is currently being processed.</p>
                        </div>
                    </div>
                    
                    <div class="assignment-details">
                        <div class="detail-row full-width">
                            <span class="detail-label"><i class="fas fa-map-marker-alt"></i> Pickup Location:</span>
                            <span class="detail-value ${childData.pickupAddress || childData.pickupCoordinates ? '' : 'pending'}">
                                ${childData.pickupAddress ? childData.pickupAddress :
            (childData.pickupCoordinates ?
                `Lat: ${childData.pickupCoordinates.latitude}, Lng: ${childData.pickupCoordinates.longitude}` :
                'Not Set')}
                            </span>
                        </div>
                        ${childData.pickupCoordinates ? `
                        <div class="detail-row">
                            <span class="detail-label"><i class="fas fa-map"></i> Latitude:</span>
                            <span class="detail-value">${childData.pickupCoordinates.latitude || 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label"><i class="fas fa-map"></i> Longitude:</span>
                            <span class="detail-value">${childData.pickupCoordinates.longitude || 'N/A'}</span>
                        </div>
                        ` : ''}
                        <div class="detail-row">
                            <span class="detail-label"><i class="fas fa-user-tie"></i> Assigned Driver:</span>
                            <span class="detail-value pending">Pending Assignment</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label"><i class="fas fa-car"></i> Vehicle:</span>
                            <span class="detail-value pending">Pending Assignment</span>
                        </div>
                    </div>
                </div>
                
                <!-- Action Buttons -->
                <div class="modal-actions">
                    <button class="btn-modal btn-close-child">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-assign-driver">
                        <i class="fas fa-user-plus"></i> Assign Driver
                    </button>
                    <button class="btn-modal btn-contact-parent">
                        <i class="fas fa-phone"></i> Contact Parent
                    </button>
                </div>
            </div>
        </div>
    `;

    // Add styles
    addChildModalStyles();

    document.body.appendChild(modal);

    // Event handlers
    const closeModal = () => {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    };

    modal.querySelector('#close-child-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-close-child').addEventListener('click', closeModal);
    modal.querySelector('.btn-assign-driver').addEventListener('click', () => {
        alert('Driver assignment feature coming soon!');
    });
    modal.querySelector('.btn-contact-parent').addEventListener('click', () => {
        const phone = childData.parentContact1 || childData.parentContact2;
        if (phone) {
            alert(`Contact ${childData.parentName} at ${phone}`);
        } else {
            alert('No contact number available');
        }
    });
    modal.addEventListener('click', (e) => {
        if (e.target.classList.contains('parent-modal-overlay')) closeModal();
    });
}

// Add child modal styles
function addChildModalStyles() {
    if (document.getElementById('child-modal-styles')) return;

    const style = document.createElement('style');
    style.id = 'child-modal-styles';
    style.textContent = `
        .modal-header-child {
            background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%);
            color: white;
            padding: 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header-child h2 {
            font-size: 1.75rem;
            font-weight: 800;
            margin: 0;
        }
        
        .child-profile-section {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: 2rem;
            margin-bottom: 2rem;
            align-items: start;
        }
        
        .child-profile-img {
            width: 150px;
            height: 150px;
            border-radius: 50%;
            border: 5px solid #EC4899;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }
        
        .status-badge-child {
            margin-top: 1rem;
            color: white;
            padding: 0.5rem 1rem;
            border-radius: 2rem;
            font-size: 0.875rem;
            font-weight: 600;
            text-align: center;
        }
        
        .child-subtitle {
            color: #64748B;
            margin: 0 0 1rem 0;
            font-size: 1rem;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 1rem;
        }
        
        .info-item .label {
            font-size: 0.75rem;
            color: #94A3B8;
            margin: 0 0 0.25rem 0;
            text-transform: uppercase;
            font-weight: 600;
        }
        
        .info-item .label i {
            color: #EC4899;
            margin-right: 0.25rem;
        }
        
        .info-item .value {
            font-size: 1rem;
            color: #1E293B;
            margin: 0;
            font-weight: 600;
        }
        
        .parent-guardian-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .parent-guardian-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .parent-guardian-section h4 i {
            color: #3B82F6;
            margin-right: 0.5rem;
        }
        
        .guardian-card {
            background: white;
            border-radius: 0.75rem;
            padding: 1.5rem;
            display: flex;
            gap: 1.5rem;
            border-left: 4px solid #3B82F6;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .guardian-avatar {
            width: 60px;
            height: 60px;
            background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.5rem;
            flex-shrink: 0;
        }
        
        .guardian-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1rem;
        }
        
        .guardian-header h5 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0;
        }
        
        .guardian-badge {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 1rem;
            font-size: 0.75rem;
            font-weight: 600;
        }
        
        .guardian-contacts {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }
        
        .contact-row {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            color: #475569;
            font-size: 0.9rem;
        }
        
        .contact-row i {
            color: #3B82F6;
            width: 20px;
        }
        
        .driver-assignment-section {
            background: white;
            padding: 1.5rem;
            border-radius: 1rem;
            border: 2px solid #FCD34D;
            margin-bottom: 2rem;
        }
        
        .driver-assignment-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .driver-assignment-section h4 i {
            color: #F59E0B;
            margin-right: 0.5rem;
        }
        
        .assignment-status {
            display: flex;
            gap: 1rem;
            padding: 1rem;
            border-radius: 0.75rem;
            margin-bottom: 1rem;
        }
        
        .assignment-status.in-progress {
            background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
            border: 1px solid #F59E0B;
        }
        
        .assignment-status .status-icon {
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.25rem;
            flex-shrink: 0;
        }
        
        .assignment-status h5 {
            font-size: 1rem;
            font-weight: 700;
            color: #92400E;
            margin: 0 0 0.25rem 0;
        }
        
        .assignment-status p {
            font-size: 0.875rem;
            color: #A16207;
            margin: 0;
        }
        
        .assignment-details {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 0.75rem;
        }
        
        .detail-row {
            display: flex;
            justify-content: space-between;
            padding: 0.75rem;
            background: #F7F9FC;
            border-radius: 0.5rem;
        }
        
        .detail-row.full-width {
            grid-column: 1 / -1;
            flex-direction: column;
            gap: 0.5rem;
        }
        
        .detail-label {
            font-size: 0.875rem;
            color: #64748B;
            font-weight: 500;
        }
        
        .detail-label i {
            color: #3B82F6;
            margin-right: 0.5rem;
        }
        
        .detail-value {
            font-size: 0.875rem;
            font-weight: 600;
            color: #1E293B;
        }
        
        .detail-value.pending {
            color: #F59E0B;
            font-style: italic;
        }
        
        .btn-assign-driver {
            background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
            color: white;
        }
        
        .btn-assign-driver:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(245, 158, 11, 0.4);
        }
        
        .btn-contact-parent {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
        }
        
        .btn-contact-parent:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
        }
        
        .btn-close-child {
            background: #64748B;
            color: white;
        }
        
        @media (max-width: 768px) {
            .child-profile-section {
                grid-template-columns: 1fr;
                text-align: center;
            }
            
            .info-grid {
                grid-template-columns: 1fr;
            }
            
            .guardian-card {
                flex-direction: column;
                text-align: center;
            }
            
            .guardian-avatar {
                margin: 0 auto;
            }
            
            .guardian-header {
                flex-direction: column;
            }
            
            .assignment-details {
                grid-template-columns: 1fr;
            }
        }
    `;
    document.head.appendChild(style);
}

// Export function
window.ParentManagement = {
    showParentDetailsModal,
    showChildDetailsModal
};
