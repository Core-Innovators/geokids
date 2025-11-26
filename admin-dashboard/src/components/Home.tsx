import { useState } from 'react';
import SideBar from '../layouts/SideBar';
import driverImage from '../assets/driver.png';
import parentImage from '../assets/parent.png';
import locationImage from '../assets/location (2).png';
import announcementImage from '../assets/annoucement.png';

function Home() {

  
  const [hoveredCard, setHoveredCard] = useState<string | null>(null);

  const containerStyle = {
    marginLeft: '250px',
    padding: '40px',
    height: '90vh',
    width: '1080px',
    backgroundColor: '#f5f5f5',
    boxSizing: 'border-box' as const,
    overflow: 'hidden'
  };

  const headerStyle = {
    textAlign: 'center' as const,
    marginBottom: '40px'
  };

  const titleStyle = {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#333'
  };

  const gridStyle = {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gridTemplateRows: '1fr 1fr',
    gap: '30px',
    maxWidth: '900px',
    height: '70vh',
    margin: '0 auto'
  };

  const getCardStyle = (isHovered: boolean) => ({
    backgroundColor: '#FFA07A',
    borderRadius: '30px',
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    transform: isHovered ? 'translateY(-5px)' : 'translateY(0)',
    transition: 'transform 0.3s ease',
    textDecoration: 'none'
  });

  const iconStyle = {
    fontSize: '80px',
    marginBottom: '15px'
  };

  const labelStyle = {
    fontSize: '20px',
    fontWeight: '600',
    color: '#333'
  };

  return (
    <><SideBar />
    <div style={containerStyle}>
      {/* Header */}
      <div style={headerStyle}>
        <h1 style={titleStyle}>GEO KIDS ADMIN DASHBOARD</h1>
      </div>

      {/* Grid */}
      <div style={gridStyle}>
        {/* Card 1 - Drivers */}
        <a
          href="/drivers"
          style={getCardStyle(hoveredCard === 'drivers')}
          onMouseEnter={() => setHoveredCard('drivers')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={iconStyle}>
           <img src={driverImage} alt="Drivers" style={{ width: "150px", height: "150px" }} />

          </div>
          <div style={labelStyle}>Drivers</div>
        </a>

        {/* Card 2 - Parents & Children */}
        <a
          href="/parent-child"
          style={getCardStyle(hoveredCard === 'parents')}
          onMouseEnter={() => setHoveredCard('parents')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={iconStyle}>
            <img src={parentImage} alt="Parents" style={{ width: "150px", height: "150px" }} />
          </div>
          <div style={labelStyle}>Parents & Children</div>
        </a>

        {/* Card 3 - Location */}
        <a
          href="/location"
          style={getCardStyle(hoveredCard === 'location')}
          onMouseEnter={() => setHoveredCard('location')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={iconStyle}>
            <img src={locationImage} alt="Location" style={{ width: "150px", height: "150px" }} />
          </div>
          <div style={labelStyle}>Location Tracking</div>
        </a>

        {/* Card 4 - Announcements */}
        <a
          href="/announcements"
          style={getCardStyle(hoveredCard === 'announcements')}
          onMouseEnter={() => setHoveredCard('announcements')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={iconStyle}>
            <img src={announcementImage} alt="Annoucement" style={{ width: "150px", height: "150px" }} />
          </div>
          <div style={labelStyle}>Announcements</div>
        </a>
      </div>
    </div></>
  );
}

export default Home;