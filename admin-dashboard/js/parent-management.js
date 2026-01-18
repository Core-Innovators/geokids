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

    console.log('📋 Showing parent details:', parentData);

    // Get children from parentData (passed from app.js)
    const children = parentData.children || [];

    // Format dates
    const createdAt = parentData.createdAt ?
        (typeof parentData.createdAt === 'number' ?
            new Date(parentData.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) :
            (parentData.createdAt.seconds ?
                new Date(parentData.createdAt.seconds * 1000).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) :
                'N/A')
        ) : 'N/A';

    const profileImage = `https://ui-avatars.com/api/?name=${encodeURIComponent(parentData.parentName || 'Parent')}&size=200&background=3B82F6&color=fff&bold=true`;

    const modal = document.createElement('div');
    modal.id = 'parent-details-modal';
    modal.className = 'parent-modal-overlay';

    modal.innerHTML = `
        <div class="modal-content-large">
            <!-- Blue Header for Parents -->
            <div class="modal-header-blue">
                <h2><i class="fas fa-users"></i> Parent & Family Details</h2>
                <button id="close-parent-modal" class="modal-close-btn">&times;</button>
            </div>
            
            <div class="modal-body-scroll">
                <!-- Parent Profile Section -->
                <div class="parent-profile-section">
                    <div class="profile-image-section">
                        <img src="${profileImage}" alt="Profile" class="parent-profile-img">
                        <div class="status-badge-parent">
                            <i class="fas fa-${parentData.status === 'active' ? 'check-circle' : 'clock'}"></i> 
                            ${parentData.status === 'active' ? 'Active' : 'Pending'}
                        </div>
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${parentData.parentName || 'N/A'}</h3>
                        <p class="parent-subtitle"><i class="fas fa-user-friends"></i> Parent/Guardian</p>
                        
                        <div class="contact-grid">
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-phone"></i> Primary Contact</p>
                                <p class="value">
                                    <a href="tel:${parentData.parentContact1 || ''}" style="color: #3B82F6; text-decoration: none;">
                                        ${parentData.parentContact1 || 'N/A'}
                                    </a>
                                </p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-phone-alt"></i> Secondary Contact</p>
                                <p class="value">${parentData.parentContact2 || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-id-card"></i> NIC Number</p>
                                <p class="value">${parentData.parentNic || 'N/A'}</p>
                            </div>
                            <div class="contact-item">
                                <p class="label"><i class="fas fa-calendar"></i> Registered On</p>
                                <p class="value">${createdAt}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Pickup Address Section -->
                <div class="address-section">
                    <h4><i class="fas fa-map-marker-alt"></i> Pickup Location</h4>
                    <div class="address-card">
                        <div class="address-icon">
                            <i class="fas fa-home"></i>
                        </div>
                        <div class="address-details">
                            <p class="address-text">${parentData.pickupAddress || 'Not Set'}</p>
                            ${parentData.pickupCoordinates ? `
                                <p class="coordinates">
                                    <i class="fas fa-map-pin"></i>
                                    Lat: ${parentData.pickupCoordinates.latitude}, Lng: ${parentData.pickupCoordinates.longitude}
                                </p>
                            ` : ''}
                        </div>
                    </div>
                </div>
                
                <!-- Children Section -->
                <div class="children-section">
                    <h4><i class="fas fa-child"></i> Registered Children (${children.length})</h4>
                    
                    ${children.length > 0 ? `
                        <div class="children-grid">
                            ${children.map(child => {
        const driverStatus = child.assignedDriver?.status || 'pending';
        const statusColor = driverStatus === 'accepted' ? '#10B981' :
            (driverStatus === 'assigned' ? '#F59E0B' : '#94A3B8');
        const statusText = driverStatus === 'accepted' ? 'Driver Confirmed' :
            (driverStatus === 'assigned' ? 'Pending Acceptance' : 'No Driver');

        return `
                                    <div class="child-card">
                                        <div class="child-header">
                                            <div class="child-avatar" style="background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%);">
                                                ${(child.childName || 'C').charAt(0).toUpperCase()}
                                            </div>
                                            <div class="child-basic-info">
                                                <h5>${child.childName || 'N/A'}</h5>
                                                <p class="child-meta">${child.childAge || 'N/A'} years old • ${child.childGrade || 'N/A'}</p>
                                            </div>
                                            <div class="child-status-badge" style="background: ${statusColor};">
                                                ${statusText}
                                            </div>
                                        </div>
                                        
                                        <div class="child-details">
                                            <div class="child-detail-item">
                                                <i class="fas fa-school"></i>
                                                <span>${child.childSchool || 'N/A'}</span>
                                            </div>
                                            ${child.assignedDriver?.driverName ? `
                                                <div class="child-detail-item">
                                                    <i class="fas fa-user-tie"></i>
                                                    <span>Driver: ${child.assignedDriver.driverName}</span>
                                                </div>
                                            ` : `
                                                <div class="child-detail-item" style="color: #F59E0B;">
                                                    <i class="fas fa-exclamation-triangle"></i>
                                                    <span>No driver assigned</span>
                                                </div>
                                            `}
                                        </div>
                                    </div>
                                `;
    }).join('')}
                        </div>
                    ` : `
                        <div class="no-children-message">
                            <i class="fas fa-child"></i>
                            <p>No children registered yet</p>
                        </div>
                    `}
                </div>
                
                <!-- Action Buttons -->
                <div class="modal-actions">
                    <button class="btn-modal btn-close-parent">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-call-parent">
                        <i class="fas fa-phone"></i> Call Parent
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
    modal.querySelector('.btn-call-parent').addEventListener('click', () => {
        const phone = parentData.parentContact1 || parentData.parentContact2;
        if (phone) {
            window.open(`tel:${phone}`, '_self');
        } else {
            alert('No phone number available');
        }
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

        .btn-call-parent {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
        }
        
        .btn-call-parent:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
        }
        
        /* Address Section */
        .address-section {
            background: linear-gradient(135deg, #F7F9FC 0%, #E5E9F2 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-bottom: 2rem;
        }
        
        .address-section h4 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 1rem 0;
        }
        
        .address-section h4 i {
            color: #EF4444;
            margin-right: 0.5rem;
        }
        
        .address-card {
            background: white;
            border-radius: 0.75rem;
            padding: 1.5rem;
            display: flex;
            gap: 1.5rem;
            border-left: 4px solid #EF4444;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .address-icon {
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.25rem;
            flex-shrink: 0;
        }
        
        .address-text {
            font-size: 1rem;
            font-weight: 600;
            color: #1E293B;
            margin: 0 0 0.5rem 0;
        }
        
        .coordinates {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
        }
        
        .coordinates i {
            color: #EF4444;
            margin-right: 0.25rem;
        }
        
        /* Child Status Badge */
        .child-status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 1rem;
            font-size: 0.65rem;
            font-weight: 600;
            color: white;
            white-space: nowrap;
        }
        
        /* No Children Message */
        .no-children-message {
            text-align: center;
            padding: 3rem;
            background: #F7F9FC;
            border-radius: 0.75rem;
            color: #94A3B8;
        }
        
        .no-children-message i {
            font-size: 3rem;
            margin-bottom: 1rem;
            display: block;
        }
        
        .no-children-message p {
            margin: 0;
            font-size: 1rem;
        }
        
        @media (max-width: 768px) {
            .parent-profile-section {
                grid-template-columns: 1fr;
                text-align: center;
            }
            
            .children-grid {
                grid-template-columns: 1fr;
            }
            
            .address-card {
                flex-direction: column;
                text-align: center;
            }
            
            .address-icon {
                margin: 0 auto;
            }
            
            .child-header {
                flex-wrap: wrap;
            }
            
            .child-status-badge {
                margin-top: 0.5rem;
                margin-left: unset;
            }
        }
    `;
    document.head.appendChild(style);
}

// Show child and parent details modal from Firebase data
async function showChildDetailsModal(childData) {
    // Remove existing modal if any
    const existingModal = document.getElementById('child-details-modal');
    if (existingModal) {
        existingModal.remove();
    }

    // Log available fields in childData for debugging
    console.log('📋 Child data fields:', Object.keys(childData));
    console.log('📋 Full child data:', childData);

    // Fetch parent details from 'parents' collection if not already present
    if (!childData.parentNic || !childData.parentContact1) {
        // Try multiple possible field names for parent reference
        const parentRef = childData.parentId || childData.userId || childData.parentUserId ||
            childData.parent_id || childData.user_id || childData.uid;

        console.log('🔍 Looking for parent with reference:', parentRef);

        if (parentRef) {
            try {
                let parentData = null;

                // Strategy 1: Try getting parent by document ID
                parentData = await window.FirebaseService.getParentById(parentRef);

                // Strategy 2: If not found, query by parentId field
                if (!parentData) {
                    console.log('🔍 Trying to query by parentId field...');
                    const snapshot = await db.collection('parents')
                        .where('parentId', '==', parentRef)
                        .limit(1)
                        .get();

                    if (!snapshot.empty) {
                        const doc = snapshot.docs[0];
                        parentData = { id: doc.id, ...doc.data() };
                        console.log('✅ Parent found by parentId field:', parentData);
                    }
                }

                if (parentData) {
                    console.log('✅ Parent data loaded:', parentData);
                    // Merge parent data into childData (check multiple possible field names)
                    childData.parentName = parentData.parentName || parentData.fullName || parentData.name || childData.parentName;
                    childData.parentNic = parentData.parentNic || parentData.nic || parentData.nicNumber || childData.parentNic;
                    childData.parentContact1 = parentData.parentContact1 || parentData.contactNumber || parentData.phone || parentData.mobile || childData.parentContact1;
                    childData.parentContact2 = parentData.parentContact2 || parentData.secondaryContact || parentData.alternateContact || childData.parentContact2;
                    childData.parentEmail = parentData.email || childData.parentEmail;
                    childData.parentAddress = parentData.pickupAddress || parentData.address || childData.parentAddress;

                    // Also get pickup location from parent data
                    childData.pickupAddress = childData.pickupAddress || parentData.pickupAddress;
                    if (!childData.pickupCoordinates && parentData.pickupCoordinates) {
                        childData.pickupCoordinates = parentData.pickupCoordinates;
                    }
                } else {
                    console.warn('⚠️ Parent not found for reference:', parentRef);
                }
            } catch (error) {
                console.warn('⚠️ Could not fetch parent details:', error);
            }
        } else {
            console.warn('⚠️ No parent reference found in child data');
        }
    } else {
        console.log('✅ Parent data already present in child document');
    }

    // Use actual profile image or generate avatar
    const profileImage = childData.childProfileImageUrl ||
        `https://ui-avatars.com/api/?name=${encodeURIComponent(childData.childName || 'Child')}&size=200&background=EC4899&color=fff&bold=true`;

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

    // Get driver assignment info
    const assignedDriver = childData.assignedDriver || {};
    const driverStatus = assignedDriver.status || childData.status || 'pending';
    const hasDriver = assignedDriver.driverId && assignedDriver.driverName;

    // Determine status display
    let statusColor, statusText, statusIcon;
    switch (driverStatus) {
        case 'accepted':
            statusColor = '#10B981';
            statusText = 'Accepted';
            statusIcon = 'check-circle';
            break;
        case 'assigned':
            statusColor = '#F59E0B';
            statusText = 'Assigned (Pending Acceptance)';
            statusIcon = 'clock';
            break;
        case 'rejected':
            statusColor = '#EF4444';
            statusText = 'Rejected';
            statusIcon = 'times-circle';
            break;
        default:
            statusColor = '#94A3B8';
            statusText = 'Pending Assignment';
            statusIcon = 'hourglass-half';
    }

    // Format driver assigned date - handle Firestore timestamp
    let assignedDate = 'N/A';
    if (assignedDriver.assignedAt) {
        try {
            let dateObj;
            if (assignedDriver.assignedAt.toDate) {
                // Firestore Timestamp object with toDate method
                dateObj = assignedDriver.assignedAt.toDate();
            } else if (assignedDriver.assignedAt.seconds) {
                // Firestore timestamp serialized as {seconds, nanoseconds}
                dateObj = new Date(assignedDriver.assignedAt.seconds * 1000);
            } else if (typeof assignedDriver.assignedAt === 'number') {
                // Unix timestamp in milliseconds
                dateObj = new Date(assignedDriver.assignedAt);
            } else if (typeof assignedDriver.assignedAt === 'string') {
                // ISO date string
                dateObj = new Date(assignedDriver.assignedAt);
            }

            if (dateObj && !isNaN(dateObj.getTime())) {
                assignedDate = dateObj.toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            }
        } catch (e) {
            console.warn('Error parsing assignedAt date:', e);
            assignedDate = 'N/A';
        }
    }
    console.log('📅 Assigned date parsed:', assignedDate, 'from:', assignedDriver.assignedAt);

    // Build driver section HTML based on status
    let driverSectionHTML = '';
    if (hasDriver) {
        if (driverStatus === 'accepted') {
            // Driver accepted - show full details with QR code section
            driverSectionHTML = `
                <div class="assignment-status accepted">
                    <div class="status-icon">
                        <i class="fas fa-check-circle"></i>
                    </div>
                    <div class="status-info">
                        <h5>Driver Confirmed</h5>
                        <p>This child has been accepted by the driver and transport is confirmed.</p>
                    </div>
                </div>
                
                <div class="driver-info-card">
                    <div class="driver-avatar-small">
                        <i class="fas fa-user-tie"></i>
                    </div>
                    <div class="driver-details-info">
                        <h5>${assignedDriver.driverName}</h5>
                        <p class="driver-id">Driver ID: ${assignedDriver.driverId.substring(0, 8)}...</p>
                    </div>
                    <div class="driver-status-badge accepted">
                        <i class="fas fa-check"></i> Confirmed
                    </div>
                </div>
                
                <div class="assignment-details">
                    <div class="detail-row">
                        <span class="detail-label"><i class="fas fa-calendar-check"></i> Assigned On:</span>
                        <span class="detail-value">${assignedDate}</span>
                    </div>
                    <div class="detail-row full-width">
                        <span class="detail-label"><i class="fas fa-map-marker-alt"></i> Pickup Location:</span>
                        <span class="detail-value ${childData.pickupAddress || childData.pickupCoordinates ? '' : 'pending'}">
                            ${childData.pickupAddress ? childData.pickupAddress :
                    (childData.pickupCoordinates ?
                        `Lat: ${childData.pickupCoordinates.latitude}, Lng: ${childData.pickupCoordinates.longitude}` :
                        'Not Set')}
                        </span>
                    </div>
                </div>
                
                <!-- QR Code Section -->
                <div class="qr-code-section">
                    <h4><i class="fas fa-qrcode"></i> Child Verification QR Code</h4>
                    <div class="qr-code-container" id="qr-code-container">
                        <div class="qr-loading">
                            <i class="fas fa-spinner fa-spin"></i>
                            <p>Loading QR Code...</p>
                        </div>
                    </div>
                    <p class="qr-description">Driver can scan this QR code to verify child details during pickup/drop-off</p>
                </div>
            `;
        } else if (driverStatus === 'assigned') {
            // Driver assigned but not yet accepted
            driverSectionHTML = `
                <div class="assignment-status pending">
                    <div class="status-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="status-info">
                        <h5>Awaiting Driver Acceptance</h5>
                        <p>A request has been sent to the driver. Waiting for confirmation.</p>
                    </div>
                </div>
                
                <div class="driver-info-card pending">
                    <div class="driver-avatar-small">
                        <i class="fas fa-user-tie"></i>
                    </div>
                    <div class="driver-details-info">
                        <h5>${assignedDriver.driverName}</h5>
                        <p class="driver-id">Driver ID: ${assignedDriver.driverId.substring(0, 8)}...</p>
                    </div>
                    <div class="driver-status-badge pending">
                        <i class="fas fa-clock"></i> Pending
                    </div>
                </div>
                
                <div class="assignment-details">
                    <div class="detail-row">
                        <span class="detail-label"><i class="fas fa-calendar-plus"></i> Request Sent:</span>
                        <span class="detail-value">${assignedDate}</span>
                    </div>
                    <div class="detail-row full-width">
                        <span class="detail-label"><i class="fas fa-map-marker-alt"></i> Pickup Location:</span>
                        <span class="detail-value ${childData.pickupAddress || childData.pickupCoordinates ? '' : 'pending'}">
                            ${childData.pickupAddress ? childData.pickupAddress :
                    (childData.pickupCoordinates ?
                        `Lat: ${childData.pickupCoordinates.latitude}, Lng: ${childData.pickupCoordinates.longitude}` :
                        'Not Set')}
                        </span>
                    </div>
                </div>
                
                <div class="qr-code-section disabled">
                    <h4><i class="fas fa-qrcode"></i> Child Verification QR Code</h4>
                    <div class="qr-placeholder">
                        <i class="fas fa-lock"></i>
                        <p>QR Code will be generated after driver accepts</p>
                    </div>
                </div>
            `;
        }
    } else {
        // No driver assigned yet
        driverSectionHTML = `
            <div class="assignment-status in-progress">
                <div class="status-icon">
                    <i class="fas fa-hourglass-half"></i>
                </div>
                <div class="status-info">
                    <h5>No Driver Assigned</h5>
                    <p>This child has not been assigned to any driver yet. Parent needs to request a driver via the mobile app.</p>
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
                <div class="detail-row">
                    <span class="detail-label"><i class="fas fa-user-tie"></i> Assigned Driver:</span>
                    <span class="detail-value pending">Not Assigned</span>
                </div>
            </div>
        `;
    }

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
                        <img src="${profileImage}" alt="Profile" class="child-profile-img" onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(childData.childName || 'Child')}&size=200&background=EC4899&color=fff&bold=true'">
                        <div class="status-badge-child" style="background: linear-gradient(135deg, ${statusColor} 0%, ${statusColor}dd 100%);">
                            <i class="fas fa-${statusIcon}"></i> ${statusText}
                        </div>
                    </div>
                    
                    <div class="profile-info-section">
                        <h3>${childData.childName || 'N/A'}</h3>
                        <p class="child-subtitle"><i class="fas fa-graduation-cap"></i> ${childData.childGrade || 'N/A'} Student</p>
                        
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
                    ${driverSectionHTML}
                </div>
                
                <!-- Action Buttons -->
                <div class="modal-actions">
                    <button class="btn-modal btn-close-child">
                        <i class="fas fa-times"></i> Close
                    </button>
                    <button class="btn-modal btn-contact-parent">
                        <i class="fas fa-phone"></i> Contact Parent
                    </button>
                    ${driverStatus === 'accepted' ? `
                    <button class="btn-modal btn-download-qr">
                        <i class="fas fa-download"></i> Download QR
                    </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;

    // Add styles
    addChildModalStyles();

    document.body.appendChild(modal);

    // Load QR code if status is accepted
    if (driverStatus === 'accepted') {
        loadChildQRCode(childData);
    }

    // Event handlers
    const closeModal = () => {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    };

    modal.querySelector('#close-child-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-close-child').addEventListener('click', closeModal);

    modal.querySelector('.btn-contact-parent').addEventListener('click', () => {
        const phone = childData.parentContact1 || childData.parentContact2;
        if (phone) {
            alert(`Contact ${childData.parentName} at ${phone}`);
        } else {
            alert('No contact number available');
        }
    });

    // Download QR button
    const downloadBtn = modal.querySelector('.btn-download-qr');
    if (downloadBtn) {
        downloadBtn.addEventListener('click', () => {
            const qrImg = modal.querySelector('#child-qr-code');
            if (qrImg && qrImg.src) {
                const link = document.createElement('a');
                link.download = `qr-code-${childData.childName.replace(/\s+/g, '-')}.png`;
                link.href = qrImg.src;
                link.click();
            } else {
                alert('QR Code is still loading...');
            }
        });
    }

    modal.addEventListener('click', (e) => {
        if (e.target.classList.contains('parent-modal-overlay')) closeModal();
    });
}

// Load and display QR code for a child
async function loadChildQRCode(childData) {
    const container = document.getElementById('qr-code-container');
    if (!container) return;

    try {
        // Get or generate QR code using QRCodeService
        const qrCodeUrl = await window.QRCodeService.getOrGenerateChildQRCode(childData);

        if (qrCodeUrl) {
            container.innerHTML = `
                <img src="${qrCodeUrl}" alt="Child QR Code" id="child-qr-code" class="qr-code-image" 
                     onerror="this.parentElement.innerHTML='<div class=\\'qr-error\\'><i class=\\'fas fa-exclamation-triangle\\'></i><p>Failed to load QR image</p></div>'">
                <div class="qr-info">
                    <p><strong>${childData.childName}</strong></p>
                    <p>${childData.childSchool} • ${childData.childGrade}</p>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="qr-error">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>QR Code not available</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading QR code:', error);
        container.innerHTML = `
            <div class="qr-error">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load QR Code</p>
                <p style="font-size: 0.7rem; color: #94A3B8;">${error.message}</p>
            </div>
        `;
    }
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
        
        .btn-download-qr {
            background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
            color: white;
        }
        
        .btn-download-qr:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(139, 92, 246, 0.4);
        }
        
        /* Driver Info Card Styles */
        .driver-info-card {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1rem;
            background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
            border-radius: 0.75rem;
            border: 1px solid #10B981;
            margin-bottom: 1rem;
        }
        
        .driver-info-card.pending {
            background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
            border-color: #F59E0B;
        }
        
        .driver-avatar-small {
            width: 50px;
            height: 50px;
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.25rem;
            flex-shrink: 0;
        }
        
        .driver-info-card.pending .driver-avatar-small {
            background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
        }
        
        .driver-details-info h5 {
            font-size: 1.125rem;
            font-weight: 700;
            color: #1E293B;
            margin: 0 0 0.25rem 0;
        }
        
        .driver-id {
            font-size: 0.75rem;
            color: #64748B;
            margin: 0;
            font-family: monospace;
        }
        
        .driver-status-badge {
            margin-left: auto;
            padding: 0.5rem 1rem;
            border-radius: 2rem;
            font-size: 0.75rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .driver-status-badge.accepted {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            color: white;
        }
        
        .driver-status-badge.pending {
            background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
            color: white;
        }
        
        /* Status Styles */
        .assignment-status.accepted {
            background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
            border: 1px solid #10B981;
        }
        
        .assignment-status.accepted .status-icon {
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
        }
        
        .assignment-status.accepted h5 {
            color: #065F46;
        }
        
        .assignment-status.accepted p {
            color: #047857;
        }
        
        .assignment-status.pending {
            background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
            border: 1px solid #F59E0B;
        }
        
        .assignment-status.pending .status-icon {
            background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
        }
        
        .assignment-status.pending h5 {
            color: #92400E;
        }
        
        .assignment-status.pending p {
            color: #A16207;
        }
        
        /* QR Code Section Styles */
        .qr-code-section {
            background: linear-gradient(135deg, #EDE9FE 0%, #DDD6FE 100%);
            padding: 1.5rem;
            border-radius: 1rem;
            margin-top: 1.5rem;
            text-align: center;
            border: 2px solid #8B5CF6;
        }
        
        .qr-code-section h4 {
            font-size: 1.125rem;
            font-weight: 700;
            color: #5B21B6;
            margin: 0 0 1rem 0;
        }
        
        .qr-code-section h4 i {
            margin-right: 0.5rem;
        }
        
        .qr-code-section.disabled {
            background: linear-gradient(135deg, #F1F5F9 0%, #E2E8F0 100%);
            border-color: #94A3B8;
        }
        
        .qr-code-section.disabled h4 {
            color: #64748B;
        }
        
        .qr-code-container {
            background: white;
            padding: 1.5rem;
            border-radius: 0.75rem;
            display: inline-block;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .qr-code-image {
            width: 200px;
            height: 200px;
            display: block;
            margin: 0 auto 1rem;
        }
        
        .qr-info {
            text-align: center;
        }
        
        .qr-info p {
            margin: 0;
            color: #475569;
            font-size: 0.875rem;
        }
        
        .qr-info p strong {
            color: #1E293B;
            font-size: 1rem;
        }
        
        .qr-loading {
            padding: 2rem;
            color: #8B5CF6;
        }
        
        .qr-loading i {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        
        .qr-loading p {
            margin: 0;
            font-size: 0.875rem;
        }
        
        .qr-error {
            padding: 2rem;
            color: #EF4444;
        }
        
        .qr-error i {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        
        .qr-error p {
            margin: 0;
            font-size: 0.875rem;
        }
        
        .qr-placeholder {
            padding: 2rem;
            background: white;
            border-radius: 0.75rem;
            color: #94A3B8;
        }
        
        .qr-placeholder i {
            font-size: 2.5rem;
            margin-bottom: 0.5rem;
        }
        
        .qr-placeholder p {
            margin: 0;
            font-size: 0.875rem;
        }
        
        .qr-description {
            margin-top: 1rem;
            font-size: 0.75rem;
            color: #7C3AED;
            font-style: italic;
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
            
            .driver-info-card {
                flex-direction: column;
                text-align: center;
            }
            
            .driver-status-badge {
                margin-left: 0;
            }
            
            .qr-code-image {
                width: 150px;
                height: 150px;
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
