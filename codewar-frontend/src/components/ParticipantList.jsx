const ParticipantList = ({ participants = [], createdBy }) => {
  if (!participants.length) {
    return <p className="text-muted">No participants yet.</p>;
  }

  return (
    <div className="participant-list">
      {participants.map((name, idx) => (
        <div key={idx} className="participant-item">
          <div className="participant-avatar">
            {name.charAt(0).toUpperCase()}
          </div>
          <span className="participant-name">
            {name}
            {name === createdBy && (
              <span className="participant-badge">Host</span>
            )}
          </span>
        </div>
      ))}
    </div>
  );
};

export default ParticipantList;
