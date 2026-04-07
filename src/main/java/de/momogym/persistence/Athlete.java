package de.momogym.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "ATHLETE")
public class Athlete {

    @Id
    @SequenceGenerator(
            name = "athlete_seq_gen",
            sequenceName = "ATHLETE_SEQ",
            initialValue = 1,
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "athlete_seq_gen")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

	@Column(name = "PASSWORD", nullable = false)
	private String password;

	@Column(name = "IS_ADMIN", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean admin = false;

    // Ein Athlet hat VIELE Pläne
    @OneToMany(
            mappedBy = "athlete",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TrainingPlan> trainingPlans = new ArrayList<>();

    // Ein Athlet hat VIELE Log-Einträge
    @OneToMany(
            mappedBy = "athlete",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExerciseLog> logs = new ArrayList<>();

	@Column(name = "PROFILE_PUBLIC")
	private boolean profilePublic = true;

	@Lob
	@Column(name = "profile_picture", length = 5242880)
	private byte[] profilePicture;

    public Athlete() {
    }
    public Athlete(String username, String password) {
        this.username = username;
		this.password = password;
    }


    public Athlete(String username, String password, List<TrainingPlan> trainingPlans, List<ExerciseLog> logs) {
        this.username = username;
		this.password = password;
        this.trainingPlans = trainingPlans;
        this.logs = logs;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public List<TrainingPlan> getTrainingPlans() {
        return trainingPlans;
    }

    public void setTrainingPlans(List<TrainingPlan> trainingPlans) {
        this.trainingPlans = trainingPlans;
    }

	public boolean isProfilePublic() {
		return profilePublic;
	}

	public void setProfilePublic(boolean profilePublic) {
		this.profilePublic = profilePublic;
	}

    public List<ExerciseLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ExerciseLog> logs) {
        this.logs = logs;
    }
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public byte[] getProfilePicture() { return profilePicture; }
	public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }

	public String getProfilePictureBase64() {
		if (this.profilePicture != null && this.profilePicture.length > 0) {
			return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(this.profilePicture);
		}
		return null;
	}
}
