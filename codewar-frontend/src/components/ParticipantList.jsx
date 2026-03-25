const ParticipantList = ({ participants = [], createdBy, cfRatings = [] }) => {
  if (!participants.length) {
    return <p className="text-muted">No participants yet.</p>;
  }

  const getRankColor = (rank) => {
    const colors = {
      newbie: "#808080",
      pupil: "#008000",
      specialist: "#03a89e",
      expert: "#0000ff",
      "candidate master": "#aa00aa",
      master: "#ff8c00",
      "international master": "#ff8c00",
      grandmaster: "#ff0000",
      "international grandmaster": "#ff0000",
      "legendary grandmaster": "#ff0000",
    };
    return colors[(rank || "").toLowerCase()] || "#94a3b8";
  };

  return (
    <div className="participant-list">
      {participants.map((name, idx) => {
        // Find matching CF user by handle (since backend doesn't return userId in cfRatings)
        const cfUser = cfRatings.find((cf) => cf.username === name);

        return (
          <div
            key={idx}
            className="participant-item"
            style={{ display: "flex", justifyContent: "space-between" }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
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

            {cfUser && (
              <div
                style={{ display: "flex", alignItems: "center", gap: "8px" }}
              >
                <span style={{ fontSize: "0.75rem", fontWeight: "600" }}>
                  Rating
                </span>
                <span
                  style={{
                    fontWeight: "700",
                    color: getRankColor(cfUser?.rank),
                  }}
                >
                  {cfUser?.rating ?? "Unrated"}
                </span>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};

export default ParticipantList;
