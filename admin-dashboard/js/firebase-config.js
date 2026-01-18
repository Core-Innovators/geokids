// ===================================
// Firebase Configuration
// ===================================

// Firebase configuration object
// TODO: Replace with your actual Firebase project configuration
const firebaseConfig = {
    apiKey: "AIzaSyCl4-Zjr60ooxuvrgwPMTFE5C0ZWziT3X0",
    authDomain: "geokids-952fc.firebaseapp.com",
    projectId: "geokids-952fc",
    storageBucket: "geokids-952fc.firebasestorage.app",
    messagingSenderId: "79781307479",
    appId: "1:79781307479:web:a52cbea847c8d4f3b1ef12"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
const auth = firebase.auth();

// ===================================
// Firebase Helper Functions
// ===================================

// Authentication
async function signInUser(email, password) {
    try {
        // const userCredential = await auth.signInWithEmailAndPassword(email, password);
        // return userCredential.user;
        console.log('Sign in function - waiting for Firebase config');
    } catch (error) {
        console.error('Error signing in:', error);
        throw error;
    }
}

async function signOutUser() {
    try {
        // await auth.signOut();
        console.log('Sign out function - waiting for Firebase config');
    } catch (error) {
        console.error('Error signing out:', error);
        throw error;
    }
}

// Firestore Operations for Drivers
async function getPendingDrivers() {
    try {
        const snapshot = await db.collection('drivers')
            .where('status', '==', 'pending')
            .get();
        // Sort by createdAt client-side to avoid composite index requirement
        const drivers = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        return drivers.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    } catch (error) {
        console.error('Error getting pending drivers:', error);
        throw error;
    }
}

// Get all drivers
async function getAllDrivers() {
    try {
        const snapshot = await db.collection('drivers')
            .orderBy('createdAt', 'desc')
            .get();
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    } catch (error) {
        console.error('Error getting all drivers:', error);
        throw error;
    }
}

// Get active/approved drivers
async function getActiveDrivers() {
    try {
        const snapshot = await db.collection('drivers')
            .where('status', '==', 'approved')
            .get();
        // Sort by createdAt client-side to avoid composite index requirement
        const drivers = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        return drivers.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    } catch (error) {
        console.error('Error getting active drivers:', error);
        throw error;
    }
}

// Get driver by ID with full details
async function getDriverById(driverId) {
    try {
        const doc = await db.collection('drivers').doc(driverId).get();
        if (!doc.exists) {
            throw new Error('Driver not found');
        }
        return { id: doc.id, ...doc.data() };
    } catch (error) {
        console.error('Error getting driver by ID:', error);
        throw error;
    }
}

async function approveDriver(driverId) {
    try {
        await db.collection('drivers').doc(driverId).update({
            status: 'approved',
            approvedAt: firebase.firestore.FieldValue.serverTimestamp(),
            updatedAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log('Driver approved:', driverId);
    } catch (error) {
        console.error('Error approving driver:', error);
        throw error;
    }
}

async function rejectDriver(driverId, rejectionReason) {
    try {
        await db.collection('drivers').doc(driverId).update({
            status: 'rejected',
            rejectionReason: rejectionReason || 'No reason provided',
            rejectedAt: firebase.firestore.FieldValue.serverTimestamp(),
            updatedAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        console.log('Driver rejected:', driverId);
    } catch (error) {
        console.error('Error rejecting driver:', error);
        throw error;
    }
}



// Firestore Operations for Parents & Children
async function getParents() {
    try {
        // const snapshot = await db.collection('parents')
        //     .orderBy('createdAt', 'desc')
        //     .get();
        // return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        console.log('Get parents - waiting for Firebase config');
        return [];
    } catch (error) {
        console.error('Error getting parents:', error);
        throw error;
    }
}

async function getChildren(parentId) {
    try {
        // const snapshot = await db.collection('children')
        //     .where('parentId', '==', parentId)
        //     .get();
        // return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        console.log('Get children - waiting for Firebase config');
        return [];
    } catch (error) {
        console.error('Error getting children:', error);
        throw error;
    }
}

// Get all registered children with their details (from children collection)
async function getRegisteredChildren() {
    try {
        // Data is stored in 'children' collection with both parent and child info
        // Fetch ALL children first for debugging, then we can add filter later
        console.log('🔍 Fetching all children from Firebase...');
        const snapshot = await db.collection('children').get();

        console.log(`📊 Total documents in children collection: ${snapshot.size}`);

        const records = snapshot.docs.map(doc => {
            const data = { id: doc.id, ...doc.data() };
            console.log('👶 Child record:', doc.id, 'Status:', data.status, 'Name:', data.childName);
            return data;
        });

        return records.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    } catch (error) {
        console.error('Error getting registered children:', error);
        throw error;
    }
}

// Get child record by ID with full details
async function getChildById(childId) {
    try {
        const doc = await db.collection('children').doc(childId).get();
        if (!doc.exists) {
            throw new Error('Record not found');
        }
        return { id: doc.id, ...doc.data() };
    } catch (error) {
        console.error('Error getting record by ID:', error);
        throw error;
    }
}

// Firestore Operations for Trips
async function getActiveTrips() {
    try {
        // const snapshot = await db.collection('trips')
        //     .where('status', '==', 'active')
        //     .orderBy('startedAt', 'desc')
        //     .get();
        // return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        console.log('Get active trips - waiting for Firebase config');
        return [];
    } catch (error) {
        console.error('Error getting active trips:', error);
        throw error;
    }
}

async function getTripHistory(startDate, endDate) {
    try {
        // const snapshot = await db.collection('trips')
        //     .where('completedAt', '>=', startDate)
        //     .where('completedAt', '<=', endDate)
        //     .orderBy('completedAt', 'desc')
        //     .get();
        // return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        console.log('Get trip history - waiting for Firebase config');
        return [];
    } catch (error) {
        console.error('Error getting trip history:', error);
        throw error;
    }
}

// Real-time Listeners
function listenToPendingDrivers(callback) {
    // return db.collection('drivers')
    //     .where('status', '==', 'pending')
    //     .onSnapshot(snapshot => {
    //         const drivers = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    //         callback(drivers);
    //     });

    console.log('Listen to pending drivers - waiting for Firebase config');
}

function listenToActiveTrips(callback) {
    // return db.collection('trips')
    //     .where('status', '==', 'active')
    //     .onSnapshot(snapshot => {
    //         const trips = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    //         callback(trips);
    //     });

    console.log('Listen to active trips - waiting for Firebase config');
}

// Statistics
async function getDashboardStats() {
    try {
        // const [drivers, parents, children, trips] = await Promise.all([
        //     db.collection('drivers').where('status', '==', 'approved').get(),
        //     db.collection('parents').get(),
        //     db.collection('children').get(),
        //     db.collection('trips').where('status', '==', 'completed').get()
        // ]);

        // return {
        //     totalDrivers: drivers.size,
        //     totalParents: parents.size,
        //     totalChildren: children.size,
        //     totalTrips: trips.size
        // };

        console.log('Get dashboard stats - waiting for Firebase config');
        return {
            totalDrivers: 24,
            totalParents: 156,
            totalChildren: 243,
            totalTrips: 1234
        };
    } catch (error) {
        console.error('Error getting dashboard stats:', error);
        throw error;
    }
}

// Get parent by ID from 'parents' collection
async function getParentById(parentId) {
    try {
        console.log('🔍 Fetching parent with ID:', parentId);
        const doc = await db.collection('parents').doc(parentId).get();
        if (!doc.exists) {
            console.log('⚠️ Parent document not found with ID:', parentId);
            return null;
        }
        const parentData = { id: doc.id, ...doc.data() };
        console.log('✅ Parent found:', parentData);
        return parentData;
    } catch (error) {
        console.error('Error getting parent by ID:', error);
        return null;
    }
}

// Get parent by userId (Firebase Auth UID) from 'parents' collection
async function getParentByUserId(userId) {
    try {
        console.log('🔍 Fetching parent with userId:', userId);

        // First try to get by document ID (if userId is the document ID)
        const docById = await db.collection('parents').doc(userId).get();
        if (docById.exists) {
            const parentData = { id: docById.id, ...docById.data() };
            console.log('✅ Parent found by doc ID:', parentData);
            return parentData;
        }

        // Otherwise query by userId field
        const snapshot = await db.collection('parents')
            .where('userId', '==', userId)
            .limit(1)
            .get();

        if (!snapshot.empty) {
            const doc = snapshot.docs[0];
            const parentData = { id: doc.id, ...doc.data() };
            console.log('✅ Parent found by userId field:', parentData);
            return parentData;
        }

        console.log('⚠️ Parent not found for userId:', userId);
        return null;
    } catch (error) {
        console.error('Error getting parent by userId:', error);
        return null;
    }
}

// Get child with full parent details
async function getChildWithParentDetails(childId) {
    try {
        // Get child data
        const childData = await getChildById(childId);
        if (!childData) return null;

        // Get parent data using parentId or userId
        const parentId = childData.parentId || childData.userId || childData.parentUserId;
        if (parentId) {
            const parentData = await getParentByUserId(parentId);
            if (parentData) {
                // Merge parent data into child data
                childData.parentName = parentData.fullName || parentData.name || childData.parentName;
                childData.parentNic = parentData.nic || parentData.nicNumber || childData.parentNic;
                childData.parentContact1 = parentData.contactNumber || parentData.phone || parentData.mobile || childData.parentContact1;
                childData.parentContact2 = parentData.secondaryContact || parentData.altPhone || childData.parentContact2;
                childData.parentEmail = parentData.email || childData.parentEmail;
                childData.parentAddress = parentData.address || childData.parentAddress;
            }
        }

        return childData;
    } catch (error) {
        console.error('Error getting child with parent details:', error);
        throw error;
    }
}

// ===================================
// Export Functions
// ===================================
window.FirebaseService = {
    signInUser,
    signOutUser,
    getPendingDrivers,
    getAllDrivers,
    getActiveDrivers,
    getDriverById,
    approveDriver,
    rejectDriver,
    getParents,
    getChildren,
    getRegisteredChildren,
    getChildById,
    getParentById,
    getParentByUserId,
    getChildWithParentDetails,
    getActiveTrips,
    getTripHistory,
    listenToPendingDrivers,
    listenToActiveTrips,
    getDashboardStats
};

console.log('📱 Firebase service initialized successfully');
console.log('✅ Connected to Firebase project:', firebaseConfig.projectId);

