import Nav from 'react-bootstrap/Nav';
function SideBar() {
  const sidebarStyle = {
    width: '250px',
    background: 'linear-gradient(180deg, #FF6B35 0%, #F7931E 100%)',
    borderRadius: '0 20px 20px 0',
    height: '100vh',
    position: 'fixed' as const,
    left: 0,
    top: 0,
    padding: '0'
  };

  const headerStyle = {
    color: 'white',
    padding: '1.5rem 1.5rem 1rem',
    marginBottom: '1rem'
  };

  const activeLinkStyle = {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    color: 'white',
    padding: '0.75rem 1rem',
    marginBottom: '0.5rem',
    borderRadius: '8px',
    display: 'flex',
    alignItems: 'center',
    textDecoration: 'none',
    transition: 'all 0.3s ease'
  };

  const linkStyle = {
    color: 'white',
    padding: '0.75rem 1rem',
    marginBottom: '0.5rem',
    borderRadius: '8px',
    display: 'flex',
    alignItems: 'center',
    textDecoration: 'none',
    transition: 'all 0.3s ease'
  };

  const iconStyle = {
    marginRight: '1rem',
    fontSize: '1.25rem'
  };

  const handleMouseEnter = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.15)';
  };

  const handleMouseLeave = (e: React.MouseEvent<HTMLAnchorElement>, isActive: boolean) => {
    e.currentTarget.style.backgroundColor = isActive ? 'rgba(255, 255, 255, 0.2)' : 'transparent';
  };

  return (
    <div style={sidebarStyle}>
      {/* Header */}
      <div style={headerStyle}>
        <h5 style={{ fontWeight: 'bold', margin: 0 }}>Admin Dashboard</h5>
      </div>

      {/* Navigation */}
      <Nav className="flex-column" style={{ padding: '0 1rem', height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
        <Nav.Link 
          href="/dashboard"
          style={activeLinkStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, true)}
        >
          <span style={iconStyle}>🏠</span>
          <span style={{ fontWeight: '600' }}>DashBoard</span>
        </Nav.Link>

        <Nav.Link 
          href="/drivers"
          style={linkStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, false)}
        >
          <span style={iconStyle}>🚗</span>
          <span style={{ fontWeight: '600' }}>Drivers</span>
        </Nav.Link>

        <Nav.Link 
          href="/parent-child"
          style={linkStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, false)}
        >
          <span style={iconStyle}>👥</span>
          <span style={{ fontWeight: '600' }}>Parent & Child</span>
        </Nav.Link>

        <Nav.Link 
          href="/trips"
          style={linkStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, false)}
        >
          <span style={iconStyle}>🗺️</span>
          <span style={{ fontWeight: '600' }}>Trips</span>
        </Nav.Link>

        <Nav.Link 
          href="/report"
          style={linkStyle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, false)}
        >
          <span style={iconStyle}>📊</span>
          <span style={{ fontWeight: '600' }}>Report</span>
        </Nav.Link>

        {/* SignOut at bottom */}
        <Nav.Link 
          href="/signout"
          style={{ ...linkStyle, marginTop: 'auto' }}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={(e) => handleMouseLeave(e, false)}
        >
          <span style={iconStyle}>🚪</span>
          <span style={{ fontWeight: '600' }}>SignOut</span>
        </Nav.Link>
      </Nav>
    </div>
  );
}

export default SideBar;