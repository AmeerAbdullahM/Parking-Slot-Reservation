# 🎓 ParkPulse: Master Viva Voce Preparation Guide
### *Comprehensive Module-by-Module Technical Defense Manual*

---

## 📌 Section 1: Architectural Foundation & Design Patterns

### Q1: Can you walk through the overall architecture of this project?
**Answer:**
ParkPulse is built on a **Layered Enterprise Architecture** with complete separation of concerns:
1. **Client / Presentation Layer:** A Single Page Application (SPA) built with React 18, utilizing Vite for fast bundling and state-driven component rendering.
2. **Security & Interception Layer:** Spring Security with a custom `JwtAuthenticationFilter` that intercepts every HTTP request, extracts the Bearer token, validates cryptographic integrity, and binds user identity into the `SecurityContext`.
3. **Controller / Web Layer (`@RestController`):** Exposes RESTful endpoints, receives incoming JSON payloads, triggers DTO validation via Jakarta Bean Validation (`@Valid`), and formats HTTP response envelopes (`ResponseEntity`).
4. **Service Layer (`@Service`):** Houses pure business logic, database transactions (`@Transactional`), conflict detection algorithms, and penalty calculations. Controllers never directly touch repositories.
5. **Persistence Layer (`@Repository`):** Built on Spring Data JPA and Hibernate ORM, abstracting relational queries, pagination, and database dialect interactions.
6. **Relational Database:** MySQL 8.0 (with H2 in-memory profile support for zero-setup evaluation).

---

### Q2: What is the purpose of Data Transfer Objects (DTOs) instead of exposing Entities directly?
**Answer:**
1. **Security & Encapsulation:** Prevents mass assignment vulnerabilities and hides sensitive internal fields (such as hashed passwords or internal database primary keys).
2. **Contract Decoupling:** Database schema changes (like renaming a table column) do not break the client-facing REST API contract.
3. **Input Validation:** Validation rules (e.g., `@NotBlank`, `@FutureOrPresent`, `@Pattern`) are applied cleanly to request DTOs without cluttering domain entities.
4. **Network Optimization:** Response DTOs aggregate exactly what the frontend needs (e.g., slot statistics, manager names) avoiding N+1 serialization loops or lazy loading exceptions.

---

### Q3: How does the Global Exception Handler work?
**Answer:**
In `GlobalExceptionHandler.java`, we annotate the class with `@RestControllerAdvice`.
* Individual exception types (e.g. `ResourceNotFoundException`, `BadRequestException`, `SlotConflictException`, `MethodArgumentNotValidException`) are annotated with `@ExceptionHandler`.
* When any service or controller throws an exception, Spring intercepts it, formats an `ErrorResponse` DTO containing the timestamp, HTTP status code, error type, descriptive message, request URI, and field-level validation errors, and returns a predictable JSON response with the proper HTTP status code (400, 403, 404, 409, 500).

---

## 💾 Section 2: Database Design & Spring Data JPA

### Q4: Explain the relationships between the database tables in ParkPulse.
**Answer:**
The schema contains **5 primary entities** plus join tables:
* **$1:N$ (One-to-Many):**
  * `ParkingLot` $\to$ `ParkingSlot`: One facility contains multiple parking bays across floors.
  * `User` $\to$ `Vehicle`: One customer can register multiple vehicles (Sedans, SUVs, EVs).
  * `User` $\to$ `Reservation`: One customer creates multiple bookings over time.
  * `ParkingSlot` $\to$ `Reservation`: One slot has multiple sequential reservations.
  * `User (Manager)` $\to$ `ParkingLot`: One manager manages their assigned facility.
* **$N:M$ (Many-to-Many):**
  * `ParkingLot` $\leftrightarrow$ `Amenity`: Modeled via the join table `parking_lot_amenities(lot_id, amenity_id)`. A facility can feature multiple amenities (EV Charging, Covered Roof, Handicap Access), and an amenity belongs to many facilities.

---

### Q5: How is the $N:M$ relationship configured in Hibernate code?
**Answer:**
In `ParkingLot.java`:
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "parking_lot_amenities",
    joinColumns = @JoinColumn(name = "lot_id"),
    inverseJoinColumns = @JoinColumn(name = "amenity_id")
)
private Set<Amenity> amenities = new HashSet<>();
```
And in `Amenity.java`:
```java
@ManyToMany(mappedBy = "amenities")
private List<ParkingLot> parkingLots = new ArrayList<>();
```
* `joinColumns` defines the foreign key referencing the owner table (`lot_id`).
* `inverseJoinColumns` defines the foreign key referencing the target table (`amenity_id`).
* `mappedBy` makes the relationship bidirectional without creating duplicate junction tables.

---

### Q6: How do you prevent double-booking for the exact same parking bay?
**Answer:**
We implement a **Temporal Overlap Collision Query** in `ReservationRepository.java`:
```sql
SELECT r FROM Reservation r 
WHERE r.slot.id = :slotId 
  AND r.status IN ('CONFIRMED', 'CHECKED_IN') 
  AND (:startTime < r.endTime AND :endTime > r.startTime)
```
* **Logic:** Two intervals $[S_1, E_1]$ and $[S_2, E_2]$ overlap if and only if:
  $$S_1 < E_2 \quad \text{AND} \quad E_1 > S_2$$
* If this query returns any records, the `ReservationService` rejects the booking immediately by throwing `SlotConflictException`, returning HTTP 409 Conflict to the client.

---

### Q7: How is Pagination implemented on the backend and frontend?
**Answer:**
* **Backend:** Controller methods accept a Spring Data `Pageable` parameter (e.g., `@PageableDefault(size = 10, sort = "createdAt") Pageable pageable`). The repository returns a `Page<T>` object containing the data slice, `totalElements`, `totalPages`, and `currentPage`.
* **Frontend:** The client passes `?page=0&size=10` query parameters via REST requests, rendering paginated tables or infinite scrolls.

---

## 🔒 Section 3: Authentication & Role-Based Access Control (RBAC)

### Q8: How does ParkPulse implement the Unified Login requirement?
**Answer:**
Rather than maintaining separate login screens for Admin, Manager, and Customer:
1. All three roles authenticate through `POST /api/auth/login` on the single `/login` page.
2. Spring Security authenticates the credentials against the database using `BCryptPasswordEncoder`.
3. If valid, the backend generates a signed JWT token embedded with the user's role (`ROLE_ADMIN`, `ROLE_MANAGER`, or `ROLE_CUSTOMER`) and their `assignedLotId`.
4. The React client reads the role claim from the returned payload and dynamically mounts the appropriate view:
   * `ROLE_ADMIN` $\to$ Admin Dashboard (Manager Provisioning & Global Metrics)
   * `ROLE_MANAGER` $\to$ Manager Console (Floor & 2D Bay Builder, Arrival Desk)
   * `ROLE_CUSTOMER` $\to$ Customer Dashboard (Facility Browser, 2D Bay Selector, Passes)

---

### Q9: Why is Parking Lot Manager registration restricted from public sign-up?
**Answer:**
As specified in our business requirements, managers oversee commercial facilities (malls, university garages) and handle money/penalty collections. If anyone could register as a manager, unauthorized users could manipulate parking rates and slot configurations.
* Therefore, only the **System Administrator** has authorization (`@PreAuthorize("hasRole('ADMIN')")`) to invoke `POST /api/admin/managers` to provision a manager identity with an initial password and link them to an official parking facility.

---

### Q10: How does BCrypt password hashing work?
**Answer:**
1. BCrypt incorporates a **128-bit random salt** generated automatically for each password, preventing rainbow table attacks.
2. It uses an adaptive key derivation function based on the Blowfish cipher with a configurable **work factor** (10 rounds in our project).
3. Even if two users enter the identical password `"password123"`, their resulting database hashes will look completely different due to the distinct salts.

---

## ⚡ Section 4: Signature Bonus Feature (Overstay Penalty Engine)

### Q11: Explain your project's unique bonus feature in detail.
**Answer:**
Our signature bonus feature is the **Automated Overstay Penalty Engine** in `OverstayPenaltyServiceImpl.java`:
1. **The Business Problem:** In malls and campuses, drivers overstaying past their booked time window block incoming traffic and prevent other booked reservations from occupying the slot.
2. **Grace Period (15 Minutes):** Drivers vacating within 15 minutes of their scheduled `end_time` receive no penalty.
3. **Calculation Formula:**
   If $\text{actualCheckOut} > \text{endTime} + 15\text{ min}$:
   $$\text{Overstay Minutes} = \text{Duration}(\text{endTime}, \text{actualCheckOut})$$
   $$\text{Billable Hours} = \lceil\text{Overstay Minutes} / 60.0\rceil$$
   $$\text{Penalty Rate} = \text{Slot Base Hourly Rate} \times 1.5$$
   $$\text{Penalty Surcharge} = \text{Billable Hours} \times \text{Penalty Rate}$$
   $$\text{Total Fee} = \text{Base Fee} + \text{Penalty Surcharge}$$
4. **State Transition:** The reservation status is updated from `CHECKED_IN` $\to$ `OVERSTAYED`, generating an itemized checkout receipt showing the overdue duration and surcharge breakdown.

---

## 🎨 Section 5: Frontend & 2D Visual Slot Map

### Q12: How is the 2D Bus-Booking-Style Parking Bay Selector implemented?
**Answer:**
* In `ParkingSlot.java`, each bay has spatial coordinates: `floorNumber`, `rowNumber`, and `colNumber`.
* The endpoint `GET /api/lots/{id}/slots/map?floor={n}` returns all slots on that floor along with capacity stats.
* In React (`CustomerDashboard.jsx`), we render a dynamic CSS Grid representing the parking deck with aerial parking stalls:
  * 🟢 **Available:** Clickable bay, opens the booking modal.
  * 🔴 **Occupied:** Disabled, indicates active vehicle.
  * ⚡ **EV Charging:** Displays neon cyan border and charging bolt icon.
  * ♿ **Handicapped / VIP:** Visual accessibility and premium indicators.
* This visual interaction mirrors airline and bus seat reservation systems, maximizing usability.

---

## 🧪 Section 6: Testing & Quality Assurance

### Q13: How did you test the service layer with Mockito?
**Answer:**
We wrote 12 unit tests using JUnit 5 and Mockito (`@ExtendWith(MockitoExtension.class)`):
* **Dependency Isolation:** We mocked `ReservationRepository`, `ParkingSlotRepository`, and `UserRepository` using `@Mock`.
* **Behavior Verification:** We used `when(...).thenReturn(...)` to simulate database responses, and `verify(..., times(1))` to assert that entities are correctly saved or rejected.
* **Edge Case Coverage:**
  * Tested on-time checkout vs. grace-period exit vs. late overstay penalty.
  * Tested duplicate bay identifier rejection within the same lot.
  * Tested duplicate customer email registration rejection.
  * Tested double-booking temporal overlap throwing `SlotConflictException`.
