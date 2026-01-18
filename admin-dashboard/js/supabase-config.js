// ===================================
// QR Code Service
// Uses QR Server API for generation (no external libraries needed)
// ===================================

/**
 * Generate QR code URL for a child using QR Server API
 * This is much simpler and more reliable than using JS libraries
 * @param {Object} childData - Child data from Firebase
 * @returns {string} - URL to QR code image
 */
function generateChildQRCodeUrl(childData) {
    // Create QR code data object with child details
    const qrData = {
        type: 'GEOKIDS_CHILD',
        childId: childData.id,
        childName: childData.childName,
        childAge: childData.childAge,
        childGrade: childData.childGrade,
        childSchool: childData.childSchool,
        parentName: childData.parentName,
        parentContact: childData.parentContact1,
        driverId: childData.assignedDriver?.driverId || null,
        driverName: childData.assignedDriver?.driverName || null,
        v: '1.0'
    };

    // Encode data for URL
    const dataString = encodeURIComponent(JSON.stringify(qrData));

    // Use QR Server API (free, reliable, no API key needed)
    // Documentation: https://goqr.me/api/
    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${dataString}&color=1E293B&bgcolor=FFFFFF&margin=10`;

    console.log('✅ QR Code URL generated for:', childData.childName);
    return qrCodeUrl;
}

/**
 * Get QR code for a child
 * If child has stored QR code URL, use it. Otherwise, generate new one.
 * @param {Object} childData - Child data from Firebase
 * @returns {Promise<string>} - QR code URL
 */
async function getOrGenerateChildQRCode(childData) {
    console.log('🔍 getOrGenerateChildQRCode called for:', childData.childName);
    console.log('📊 Status check:', {
        rootStatus: childData.status,
        driverStatus: childData.assignedDriver?.status,
        hasAssignedDriver: !!childData.assignedDriver
    });

    // Check if child already has a QR code stored in Firebase
    if (childData.qrCodeUrl) {
        console.log('📱 Using existing QR code URL from Firebase');
        return childData.qrCodeUrl;
    }

    // Check if status is 'accepted' (in assignedDriver object or at root level)
    const driverStatus = childData.assignedDriver?.status;
    const rootStatus = childData.status;
    const isAccepted = driverStatus === 'accepted' || rootStatus === 'accepted';

    if (!isAccepted) {
        console.log('⏳ QR code not generated - child not yet accepted by driver');
        return null;
    }

    // Generate QR code URL
    const qrCodeUrl = generateChildQRCodeUrl(childData);

    // Store the QR code URL in Firebase for future use
    try {
        await saveQRCodeUrlToFirebase(childData.id, qrCodeUrl);
    } catch (error) {
        console.warn('⚠️ Could not save QR code URL to Firebase:', error.message);
        // Continue anyway - we can still display the QR code
    }

    return qrCodeUrl;
}

/**
 * Save QR code URL to Firebase for the child
 * @param {string} childId - Firebase child document ID
 * @param {string} qrCodeUrl - QR code URL
 */
async function saveQRCodeUrlToFirebase(childId, qrCodeUrl) {
    try {
        await db.collection('children').doc(childId).update({
            qrCodeUrl: qrCodeUrl,
            qrCodeGeneratedAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log('✅ QR code URL saved to Firebase for child:', childId);
    } catch (error) {
        console.error('❌ Error saving QR code URL to Firebase:', error);
        throw error;
    }
}

// ===================================
// Export Functions
// ===================================
window.QRCodeService = {
    generateChildQRCodeUrl,
    getOrGenerateChildQRCode,
    saveQRCodeUrlToFirebase
};

console.log('� QR Code Service initialized (using QR Server API)');
