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

// Export function
window.ParentManagement = {
    showParentDetailsModal
};
