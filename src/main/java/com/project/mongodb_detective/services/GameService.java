package com.project.mongodb_detective.services;


import com.project.mongodb_detective.models.*;
import com.project.mongodb_detective.models.enums.RelationType;
import com.project.mongodb_detective.models.enums.WeaponType;
import com.project.mongodb_detective.repositories.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GameService {
    @Autowired
    private AutopsyRecordRepository autopsyRecordRepo;
    @Autowired
    private CameraLogRepository cameraLogRepo;
    @Autowired
    private CaseRepository caseRepo;
    @Autowired
    private CitizenRepository citizenRepo;
    @Autowired
    private CommunicationRepository communicationRepo;
    @Autowired
    private GpsLogRepository gpsLogRepo;
    @Autowired
    private TestimonyRepository testimonyRepo;
    @Autowired
    private TransactionRepository transactionRepo;
    @Autowired
    private WeaponLicenseRepository weaponLicenseRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    /* for lab work
    private void applyCitizenSchemaValidator() {
        if (mongoTemplate.collectionExists(Citizen.class)) {
            return;
        }

        MongoJsonSchema schema = MongoJsonSchema.builder()
                .required("caseId", "fullName", "age", "gender")
                .properties(
                        JsonSchemaProperty.string("caseId")
                                .description("ID справи є обов'язковим рядком"),

                        JsonSchemaProperty.string("fullName")
                                .description("ПІБ жителя є обов'язковим рядком"),

                        JsonSchemaProperty.int32("age")
                                .gte(18)
                                .lte(120)
                                .description("Вік має бути числом від 18 до 120"),

                        JsonSchemaProperty.bool("gender")
                                .description("Стать є обов'язковим булевим полем"),

                        JsonSchemaProperty.string("phone"),
                        JsonSchemaProperty.string("bankAccount"),
                        JsonSchemaProperty.bool("isSuspect"),

                        JsonSchemaProperty.array("physicalTraits")
                                .items(JsonSchemaProperty.string("trait")),

                        JsonSchemaProperty.object("vehicle")
                                .properties(
                                        JsonSchemaProperty.string("color"),
                                        JsonSchemaProperty.string("plate")
                                ),

                        JsonSchemaProperty.array("relationships")
                                .items(
                                        JsonSchemaProperty.object("relationship")
                                                .required("citizenId", "relationType", "affinity")
                                                .properties(
                                                        JsonSchemaProperty.string("citizenId"),
                                                        JsonSchemaProperty.string("relationType"),
                                                        JsonSchemaProperty.int32("affinity").gte(-5).lte(5)
                                                )
                                )
                )
                .build();

        CollectionOptions options = CollectionOptions.empty().schema(schema);
        mongoTemplate.createCollection(Citizen.class, options);
    }
     */

    Faker faker = new Faker();

    public Case generateNewCase(){
        Case gameCase = new Case();
        gameCase.setStatus("GENERATING");
        gameCase = caseRepo.save(gameCase);

        List<Citizen> people = generateCitizen(gameCase.getId(), 500);
        List<Citizen> pool = new ArrayList<>(people);
        Collections.shuffle(pool);
        Citizen victim = pool.remove(0);
        Citizen killer = pool.remove(0);
        Citizen caller = pool.remove(0);
        List<Citizen> decoys = new ArrayList<>();
        gameCase.setVictimId(victim.getId());
        gameCase.setKillerId(killer.getId());
        List<String> decoyIds = decoys.stream().map(Citizen::getId).toList();
        gameCase.setDecoyIds(decoyIds);

        int limit = faker.number().numberBetween(2, 4);
        for (int i = 0; i < limit; i++) {
            decoys.add(pool.remove(0));
        }

        if (!areConnected(victim, killer)) {
            addRelationship(victim, killer.getId(), faker.options().nextElement(new RelationType[]{RelationType.COLLEAGUE, RelationType.FRIEND, RelationType.NEIGHBOR}), faker.number().numberBetween(-5, -1));
            addRelationship(killer, victim.getId(), faker.options().nextElement(new RelationType[]{RelationType.COLLEAGUE, RelationType.FRIEND, RelationType.NEIGHBOR}), faker.number().numberBetween(-5, -1));
        }

        for (Citizen d : decoys) {
            if (!areConnected(d, victim)) {
                addRelationship(d, victim.getId(), faker.options().nextElement(new RelationType[]{RelationType.COLLEAGUE, RelationType.FRIEND, RelationType.NEIGHBOR}), faker.number().numberBetween(-5, -1));
                addRelationship(victim, d.getId(), faker.options().nextElement(new RelationType[]{RelationType.COLLEAGUE, RelationType.FRIEND, RelationType.NEIGHBOR}), faker.number().numberBetween(-5, -1));
            }
        }

        citizenRepo.saveAll(people);

        AutopsyRecord autopsy = generateAutopsy(gameCase.getId(), victim);

        generateWeaponLicense(gameCase.getId(), killer, decoys, people, autopsy);

        double x = faker.number().randomDouble(5, -100, 100);
        double y = faker.number().randomDouble(5, -100, 100);
        List<Citizen> bystanders = generateGpsLogs(gameCase.getId(), people, killer, victim, caller, decoys, autopsy.getEstimatedDeathTime(), x, y);
        List<String> bystanderIds = bystanders.stream().map(Citizen::getId).toList();
        gameCase.setBystanderIds(bystanderIds);
        generateCameraLogs(gameCase.getId(), killer, victim, decoys, bystanders, people, autopsy.getEstimatedDeathTime(), x, y);

        String motive = faker.options().nextElement(new String[]{
                "BLACKMAIL",
                "RAGE",
                "PSYCHO",
        });
        gameCase.setMotive(motive);
        generateTransactions(gameCase.getId(), killer, victim, bystanders, people, autopsy.getEstimatedDeathTime(), motive);
        generateCommunications(gameCase.getId(), killer, victim, decoys, people, autopsy.getEstimatedDeathTime(), motive);

        gameCase.setStatus("READY");
        return caseRepo.save(gameCase);
    }

    public List<Citizen> generateCitizen(String caseId, int population){
        List<Citizen> people = new ArrayList<>();
        for(int i = 0; i < population; i++){
            Citizen c = new Citizen();
            c.setCaseId(caseId);
            c.setAge(faker.number().numberBetween(18, 80));
            c.setPhone(faker.phoneNumber().cellPhone());
            c.setBankAccount(faker.finance().iban());
            c.setGender(faker.bool().bool());
            if (c.getGender()) {
                c.setFullName(faker.resolve("name.male_first_name") + " " + faker.name().lastName());
            } else {
                c.setFullName(faker.resolve("name.female_first_name") + " " + faker.name().lastName());
            }

            if(faker.number().numberBetween(0, 100) > 80){
                c.setPhysicalTraits(getRandomTraits(faker.number().numberBetween(1, 2)));
            }

            if(faker.number().numberBetween(0, 100) > 80){
                Citizen.Vehicle vehicle = new Citizen.Vehicle();
                vehicle.setColor(faker.color().name());
                vehicle.setPlate(faker.bothify("??-####"));
                c.setVehicle(vehicle);
            }

            people.add(c);
        }

        people = citizenRepo.saveAll(people);
        generateRelationships(people);

        return citizenRepo.saveAll(people);
    }

    public void generateRelationships(List<Citizen> citizens) {
        Random random = new Random();
        Set<String> marriedCitizens = new HashSet<>();

        for (int i = 0; i < citizens.size(); i++) {
            Citizen person1 = citizens.get(i);

            if (marriedCitizens.contains(person1.getId())) continue;

            for (int j = i + 1; j < citizens.size(); j++) {
                Citizen person2 = citizens.get(j);
                if (marriedCitizens.contains(person2.getId())) continue;

                if ((person1.getGender() != person2.getGender()) && (Math.abs(person1.getAge() - person2.getAge()) <= 7)) {
                    if (random.nextInt(100) < 30) {

                        RelationType typeForPerson1 = person1.getGender() ? RelationType.WIFE : RelationType.HUSBAND;
                        RelationType typeForPerson2 = person2.getGender() ? RelationType.WIFE : RelationType.HUSBAND;

                        addRelationship(person1, person2.getId(), typeForPerson1, faker.number().numberBetween(1, 5));
                        addRelationship(person2, person1.getId(), typeForPerson2, faker.number().numberBetween(1, 5));

                        if (person1.getGender()) {
                            String husbandLastName = getLastName(person1.getFullName());
                            person2.setFullName(getFirstName(person2.getFullName()) + " " + husbandLastName);
                        } else {
                            String husbandLastName = getLastName(person2.getFullName());
                            person1.setFullName(getFirstName(person1.getFullName()) + " " + husbandLastName);
                        }
                        marriedCitizens.add(person1.getId());
                        marriedCitizens.add(person2.getId());
                        break;
                    }
                }
            }
        }

        List<Citizen> workingClass = new ArrayList<>(citizens);
        Collections.shuffle(workingClass);

        int groupSize = 6;
        for (int i = 0; i < workingClass.size(); i += groupSize) {
            int end = Math.min(i + groupSize, workingClass.size());
            List<Citizen> workGroup = workingClass.subList(i, end);

            if (random.nextInt(100) < 30) {
                if (workGroup.size() > 2) {
                    Citizen boss = workGroup.get(0);

                    for (int j = 1; j < workGroup.size(); j++) {
                        Citizen employee = workGroup.get(j);

                        int x = faker.number().numberBetween(-5, 5);
                        addRelationship(employee, boss.getId(), RelationType.BOSS, x);
                        addRelationship(boss, employee.getId(), RelationType.COLLEAGUE, x);

                        for (int k = j + 1; k < workGroup.size(); k++) {
                            Citizen otherEmployee = workGroup.get(k);

                            x = faker.number().numberBetween(-5, 5);
                            addRelationship(employee, otherEmployee.getId(), RelationType.COLLEAGUE, x);
                            addRelationship(otherEmployee, employee.getId(), RelationType.COLLEAGUE, x);
                        }
                    }
                }
            }
        }

        for (Citizen citizen : citizens) {
            int friendsCount = random.nextInt(2) + 1;

            for (int k = 0; k < friendsCount; k++) {
                Citizen randomFriend = citizens.get(random.nextInt(citizens.size()));

                if (!randomFriend.getId().equals(citizen.getId()) && !hasRelationWith(citizen, randomFriend.getId())) {

                    int x = faker.number().numberBetween(1, 5);
                    if (Math.abs(citizen.getAge() - randomFriend.getAge()) <= 10) {
                        addRelationship(citizen, randomFriend.getId(), RelationType.FRIEND, x);
                        addRelationship(randomFriend, citizen.getId(), RelationType.FRIEND, x);
                    }
                }
            }
        }
    }

    private AutopsyRecord generateAutopsy(String caseId, Citizen victim) {
        List<AutopsyRecord> records = new ArrayList<>();

        AutopsyRecord realRecord = new AutopsyRecord();
        realRecord.setCaseId(caseId);
        realRecord.setVictimId(victim.getId());

        LocalDateTime crimeTime = LocalDateTime.now().minusHours(faker.number().numberBetween(6, 24));
        realRecord.setEstimatedDeathTime(crimeTime);

        WeaponType cause = faker.options().nextElement(WeaponType.values());
        realRecord.setCauseOfDeath("Смерть спричинена " + cause.getDescription());
        realRecord.setWeaponType(cause);

        records.add(realRecord);

        int noiseCount = faker.number().numberBetween(3, 5);
        for (int i = 0; i < noiseCount; i++) {
            AutopsyRecord noiseRecord = new AutopsyRecord();
            noiseRecord.setCaseId(caseId);
            noiseRecord.setVictimId("ARC-" + faker.number().digits(5));

            String deathCause = faker.options().nextElement(new String[]{
                    "природними причинами",
                    "травмами внаслідок аварії",
                    "передозування шкідливими речовинами"
            });

            noiseRecord.setCauseOfDeath("Смерть спричинена " + deathCause);
            noiseRecord.setWeaponType(WeaponType.OTHER);
            noiseRecord.setEstimatedDeathTime(crimeTime.minusHours(faker.number().numberBetween(12, 72)));

            records.add(noiseRecord);
        }

        Collections.shuffle(records);

        autopsyRecordRepo.saveAll(records);

        return realRecord;
    }

    private void generateWeaponLicense(String caseId, Citizen killer, List<Citizen> decoys, List<Citizen> people, AutopsyRecord autopsy) {
        List<WeaponLicense> licenses = new ArrayList<>();
        Set<String> keyRolesIds = new HashSet<>();
        keyRolesIds.add(killer.getId());
        for (Citizen d : decoys) {
            keyRolesIds.add(d.getId());
        }
        WeaponType killerWeapon = autopsy.getWeaponType();

        if (killerWeapon != WeaponType.KNIFE) {
            if(faker.bool().bool()) {
                licenses.add(createLicense(caseId, killer, killerWeapon));
            }
            else{
                Citizen related = findAnyRelation(killer, people).orElse(killer);
                licenses.add(createLicense(caseId, related, killerWeapon));
            }

            for (Citizen d : decoys){
                if(faker.number().numberBetween(0, 100) < 30){
                    licenses.add(createLicense(caseId, d, killerWeapon));
                }
            }

        }

        for (Citizen citizen : people) {
            if (keyRolesIds.contains(citizen.getId())) continue;

            if (faker.number().numberBetween(0, 100) < 10) {
                WeaponType randomWeapon = faker.options().nextElement(WeaponType.values());
                if (randomWeapon != WeaponType.KNIFE && randomWeapon != WeaponType.OTHER) {
                    licenses.add(createLicense(caseId, citizen, randomWeapon));
                }
            }
        }

        weaponLicenseRepo.saveAll(licenses);
    }

    public List<Citizen> generateGpsLogs(
            String caseId,
            List<Citizen> people,
            Citizen killer,
            Citizen victim,
            Citizen caller,
            List<Citizen> decoys,
            LocalDateTime crimeTime,
            double epicenterX,
            double epicenterY
    ) {
        List<GpsLog> allLogs = new ArrayList<>();

        Set<String> keyRoleIds = new HashSet<>();
        keyRoleIds.add(killer.getId());
        keyRoleIds.add(victim.getId());
        keyRoleIds.add(caller.getId());
        for (Citizen d : decoys) {
            keyRoleIds.add(d.getId());
        }


        allLogs.add(createGpsLog(caseId, victim.getId(), epicenterX, epicenterY, crimeTime));

        allLogs.add(createGpsLog(caseId, killer.getId(),
                epicenterX + faker.number().randomDouble(5, -15, 15),
                epicenterY + faker.number().randomDouble(5, -15, 15),
                crimeTime.plusMinutes(faker.number().numberBetween(-10, 10))));

        for (Citizen decoy : decoys) {
            allLogs.add(createGpsLog(caseId, decoy.getId(),
                    epicenterX + faker.number().randomDouble(5, -15, 15),
                    epicenterY + faker.number().randomDouble(5, -15, 15),
                    crimeTime.plusMinutes(faker.number().numberBetween(-10, 10))));
        }

        allLogs.add(createGpsLog(caseId, caller.getId(),
                epicenterX, epicenterY,
                crimeTime.plusMinutes(faker.number().numberBetween(45, 60))));

        List<Citizen> bystanders = new ArrayList<>();

        for (Citizen citizen : people) {
            int logCount = faker.number().numberBetween(4, 8);
            boolean isVictim = citizen.getId().equals(victim.getId());

            for (int i = 0; i < logCount; i++) {
                int minutesOffset = isVictim ? faker.number().numberBetween(-720, -1) : faker.number().numberBetween(-720, 720);

                LocalDateTime randomTime = crimeTime.plusMinutes(minutesOffset);

                boolean isBystander = !keyRoleIds.contains(citizen.getId()) && Math.abs(java.time.Duration.between(randomTime, crimeTime).toMinutes()) <= 15
                        && faker.number().numberBetween(0, 100) < 2;

                double x, y;
                if (isBystander) {
                    x = epicenterX + faker.number().randomDouble(5, -15, 15);
                    y = epicenterY + faker.number().randomDouble(5, -15, 15);

                } else {
                    x = epicenterX + faker.number().randomDouble(5, -300, 300);
                    y = epicenterY + faker.number().randomDouble(5, -300, 300);
                }

                boolean inCrimeZone = Math.abs(x - epicenterX) <= 15 && Math.abs(y - epicenterY) <= 15;
                boolean inCrimeTime = Math.abs(java.time.Duration.between(randomTime, crimeTime).toMinutes()) <= 15;

                if (!keyRoleIds.contains(citizen.getId()) && inCrimeZone && inCrimeTime) {
                    if (!bystanders.contains(citizen)) {
                        bystanders.add(citizen);
                    }
                }

                allLogs.add(createGpsLog(caseId, citizen.getId(), x, y, randomTime));
            }
        }

        gpsLogRepo.saveAll(allLogs);

        return bystanders;
    }

    private void generateCameraLogs(
            String caseId,
            Citizen killer,
            Citizen victim,
            List<Citizen> decoys,
            List<Citizen> bystanders,
            List<Citizen> people,
            LocalDateTime crimeTime,
            double epicenterX,
            double epicenterY
    ) {
        List<CameraLog> logs = new ArrayList<>();

        List<Citizen> subjectsNearScene = new ArrayList<>();
        subjectsNearScene.add(killer);
        subjectsNearScene.addAll(decoys);
        subjectsNearScene.addAll(bystanders);

        Set<String> sceneSubjectIds = subjectsNearScene.stream()
                .map(Citizen::getId)
                .collect(Collectors.toSet());

        for (Citizen person : subjectsNearScene) {
            if (faker.number().numberBetween(0, 100) < 80) {
                CameraLog log = new CameraLog();
                log.setCaseId(caseId);
                log.setTimestamp(crimeTime.plusMinutes(faker.number().numberBetween(-10, 10)));

                log.setX(epicenterX + faker.number().randomDouble(5, -20, 20));
                log.setY(epicenterY + faker.number().randomDouble(5, -20, 20));
                log.setDescription(buildDescription(person));
                logs.add(log);
            }
        }

        int noiseCount = faker.number().numberBetween(6, 12);

        for (int i = 0; i < noiseCount; i++) {
            Citizen noisePerson = people.get(faker.number().numberBetween(0, people.size()));

            if (sceneSubjectIds.contains(noisePerson.getId())) {
                continue;
            }

            CameraLog log = new CameraLog();
            log.setCaseId(caseId);
            LocalDateTime logTime = crimeTime.plusMinutes(faker.number().numberBetween(-360, 360));
            double x = epicenterX + faker.number().randomDouble(5, -300, 300);
            double y = epicenterY + faker.number().randomDouble(5, -300, 300);

            if (noisePerson.getId().equals(victim.getId()) && logTime.isAfter(crimeTime)) {
                continue;
            }

            boolean accidentalZoneHit = Math.abs(x - epicenterX) <= 20 && Math.abs(y - epicenterY) <= 20;
            boolean accidentalTimeHit = Math.abs(java.time.Duration.between(logTime, crimeTime).toMinutes()) <= 15;

            if (accidentalZoneHit && accidentalTimeHit) {
                continue;
            }

            log.setTimestamp(logTime);
            log.setX(x);
            log.setY(y);
            log.setDescription(buildDescription(noisePerson));
            logs.add(log);
        }
        cameraLogRepo.saveAll(logs);
    }

    private void generateTransactions(
            String caseId,
            Citizen killer,
            Citizen victim,
            List<Citizen> bystanders,
            List<Citizen> people,
            LocalDateTime crimeTime,
            String motive
    ) {
        List<Transaction> transactions = new ArrayList<>();

        for (Citizen bystander : bystanders) {
            Transaction alibiTx = new Transaction();
            alibiTx.setCaseId(caseId);
            alibiTx.setFromAccount(bystander.getBankAccount());
            alibiTx.setToAccount("MERCHANT-" + faker.number().digits(4)); // Касса/Кафе
            alibiTx.setAmount(faker.number().randomDouble(2, 3, 15)); // $3.50 - $15.00
            alibiTx.setTimestamp(crimeTime.minusMinutes(faker.number().numberBetween(2, 7)));
            alibiTx.setPurpose("Оплата: " + faker.options().nextElement(new String[]{"Кав'ярня", "АЗС", "Продуктовий маркет"}));
            transactions.add(alibiTx);
        }

        if(motive.equals("BLACKMAIL")){
            for (int i = 0; i < faker.number().numberBetween(2, 5); i++) {
                Transaction motiveTx = new Transaction();
                motiveTx.setCaseId(caseId);
                motiveTx.setFromAccount(victim.getBankAccount());
                motiveTx.setToAccount(killer.getBankAccount());
                motiveTx.setAmount(faker.number().randomDouble(2, 200, 1000));
                motiveTx.setTimestamp(crimeTime.minusDays(faker.number().numberBetween(1, 30)));
                motiveTx.setPurpose("Переказ коштів");
                transactions.add(motiveTx);
            }
        }

        for (Citizen p : people){
            if(faker.number().numberBetween(0, 100) < 15){
                Transaction randomTx = new Transaction();
                randomTx.setCaseId(caseId);
                randomTx.setFromAccount(p.getBankAccount());
                randomTx.setToAccount("MERCHANT-" + faker.number().digits(4));
                randomTx.setAmount(faker.number().randomDouble(2, 5, 50));
                randomTx.setTimestamp(crimeTime.minusDays(faker.number().numberBetween(1, 30)));
                randomTx.setPurpose("Оплата: " + faker.options().nextElement(new String[]{"Кав'ярня", "АЗС", "Продуктовий маркет"}));
                transactions.add(randomTx);
            }
        }

        transactionRepo.saveAll(transactions);
    }

    private void generateCommunications(
            String caseId,
            Citizen killer,
            Citizen victim,
            List<Citizen> decoys,
            List<Citizen> people,
            LocalDateTime crimeTime,
            String motive
    ) {
        List<Communication> logs = new ArrayList<>();

        if (motive.equals("BLACKMAIL")) {
            for (int i = 0; i < faker.number().numberBetween(3, 6); i++) {
                logs.add(createCommunication(caseId, killer.getPhone(), victim.getPhone(),
                        crimeTime.minusDays(faker.number().numberBetween(1, 14)),
                        "SMS", 0, faker.options().nextElement(new String[]{"Де гроші?", "Час вийшов.", "Останнє попередження.", "Ти знаєш, що буде."})));
            }
        } else if (motive.equals("RAGE")) {
            for (int i = 0; i < faker.number().numberBetween(4, 8); i++) {
                logs.add(createCommunication(caseId, killer.getPhone(), victim.getPhone(),
                        crimeTime.minusHours(faker.number().numberBetween(1, 24)),
                        "CALL", faker.number().numberBetween(5, 30), null));

                logs.add(createCommunication(caseId, victim.getPhone(), killer.getPhone(),
                        crimeTime.minusHours(faker.number().numberBetween(1, 24)),
                        "SMS", 0, "Дай мені спокій!"));
            }
        } else if (motive.equals("PSYCHO")) {
            for (int i = 0; i < faker.number().numberBetween(10, 20); i++) {
                logs.add(createCommunication(caseId, killer.getPhone(), victim.getPhone(),
                        crimeTime.minusHours(faker.number().numberBetween(1, 48)),
                        "CALL", 0, null));

                if (faker.number().numberBetween(0, 100) < 30) {
                    logs.add(createCommunication(caseId, killer.getPhone(), victim.getPhone(),
                            crimeTime.minusHours(faker.number().numberBetween(1, 48)),
                            "SMS", 0, faker.options().nextElement(new String[]{"Я бачу тебе.", "Ти від мене не сховаєшся.", "Ми будемо разом."})));
                }
            }
        }

        for (Citizen decoy : decoys) {
            String targetPhone = findAnyRelation(decoy, people)
                    .map(Citizen::getPhone)
                    .orElse(people.get(faker.number().numberBetween(0, people.size())).getPhone());

            logs.add(createCommunication(caseId, decoy.getPhone(), targetPhone,
                    crimeTime.plusMinutes(faker.number().numberBetween(-5, 5)),
                    "CALL", faker.number().numberBetween(300, 1200), null));
        }

        for (Citizen citizen : people) {
            if (faker.number().numberBetween(0, 100) < 20 && citizen.getRelationships() != null) {
                for (Citizen.Relationship rel : citizen.getRelationships()) {
                    if (faker.bool().bool()) continue;

                    String targetPhone = people.stream()
                            .filter(p -> p.getId().equals(rel.getCitizenId()))
                            .map(Citizen::getPhone)
                            .findFirst()
                            .orElse(null);

                    if (targetPhone != null) {
                        boolean isPositive = rel.getAffinity() > 0;
                        String type = faker.options().nextElement(new String[]{"CALL", "SMS"});
                        String msg = null;
                        int duration = 0;

                        if (type.equals("SMS")) {
                            msg = isPositive ? faker.options().nextElement(new String[]{"Привіт! Як справи?", "Купи хліб", "Буду о 19:00"})
                                    : faker.options().nextElement(new String[]{"Не дзвони мені більше.", "Гроші поверни"});
                        } else {
                            duration = isPositive ? faker.number().numberBetween(60, 1200) : faker.number().numberBetween(5, 30);
                        }

                        logs.add(createCommunication(caseId, citizen.getPhone(), targetPhone,
                                crimeTime.minusHours(faker.number().numberBetween(1, 72)),
                                type, duration, msg));
                    }
                }
            }
        }

        communicationRepo.saveAll(logs);
    }

    public Testimony interrogateCitizen(String caseId, String speakerId, String aboutId) {
        Optional<Testimony> existingTestimony = testimonyRepo.findByCaseIdAndSpeakerCitizenIdAndAboutCitizenId(caseId, speakerId, aboutId);
        if (existingTestimony.isPresent()) {
            return existingTestimony.get();
        }

        Citizen speaker = citizenRepo.findById(speakerId).orElseThrow();
        Citizen target = citizenRepo.findById(aboutId).orElseThrow();
        Case gameCase = caseRepo.findById(caseId).orElseThrow();

        String statementText = generateStatement(speaker, target, gameCase);

        Testimony newTestimony = new Testimony();
        newTestimony.setCaseId(caseId);
        newTestimony.setSpeakerCitizenId(speakerId);
        newTestimony.setAboutCitizenId(aboutId);
        newTestimony.setStatement(statementText);

        return testimonyRepo.save(newTestimony);
    }


    private String generateStatement(Citizen witness, Citizen target, Case gameCase) {
        StringBuilder stmt = new StringBuilder();
        boolean isSelf = witness.getId().equals(target.getId());

        String killerId = gameCase.getKillerId();
        String victimId = gameCase.getVictimId();
        boolean isTargetKiller = target.getId().equals(killerId);
        boolean isTargetDecoy = gameCase.getDecoyIds().contains(target.getId());
        boolean isTargetBystander = gameCase.getBystanderIds().contains(target.getId());
        boolean isTargetRegular = !isTargetKiller && !isTargetDecoy && !isTargetBystander && !target.getId().equals(victimId);

        Random targetRnd = new Random(target.getId().hashCode());

        String[] regularAlibis = {"був у кіно", "працював в офісі", "гуляв у парку", "був у спортзалі", "відпочивав удома"};
        String regularAlibi = regularAlibis[Math.abs(targetRnd.nextInt()) % regularAlibis.length];

        String[] killerAlibis = {"йшов додому і заходив щось купувати", "був зі знайомими", "просто гуляв по місту", "NULL"};
        String killerAlibi = killerAlibis[Math.abs(targetRnd.nextInt()) % killerAlibis.length];
        boolean hasAliby = !killerAlibi.equals("NULL");

        String[] decoySecrets = {"у барі", "в готелі"};
        String decoySecret = decoySecrets[Math.abs(targetRnd.nextInt()) % decoySecrets.length];

        if (!isSelf) {
            Optional<Citizen.Relationship> relation = witness.getRelationships() != null
                    ? witness.getRelationships().stream().filter(r -> r.getCitizenId().equals(target.getId())).findFirst()
                    : Optional.empty();

            if (relation.isEmpty()) {
                return "Я взагалі не знаю цю людину. Без поняття, де вона була.";
            }

            int affinity = relation.get().getAffinity();

            if (witness.getId().equals(killerId) && target.getId().equals(victimId)) {
                stmt.append("Ми були чудовими друзями, це така трагедія... ");
            } else {
                if(target.getId().equals(killerId) && gameCase.getMotive().equals("PSYCHO") && faker.bool().bool()){
                    stmt.append("Якщо чесно у нього дуже дивна поведінка. ");
                }
                else {
                    if (affinity >= 3) {
                        stmt.append("Це дуже хороша людина, ми чудово спілкуємося. ");
                    } else if (affinity > 0) {
                        stmt.append("Нормальна людина, іноді бачимося. ");
                    } else if (affinity <= -3) {
                        stmt.append("Жахлива людина. Якщо чесно, ми терпіти один одного не можемо. ");
                    } else {
                        stmt.append("Ми знайомі, але стосунки в нас натягнуті. ");
                    }
                }
            }
        }


        if (isSelf) {
            if (isTargetKiller) {
                if(!hasAliby) return "Я не маю до цього відношення. Мене там не було";
                else return "Я не маю до цього відношення. У той час я " + killerAlibi + ".";
            }
            else if (isTargetDecoy) {
                if(faker.bool().bool()) return "Я був "+decoySecret+".";
                else return "Я був удома весь вечір, нікуди не виходив і нічого не бачив.";
            }
            else if (isTargetBystander) {
                return "Я просто йшов додому і зайшов по дорозі щось купити. Нічого підозрілого не бачив.";
            }
            else if (isTargetRegular) {
                return "Я не причетний. У той час я " + regularAlibi + ".";
            }

        } else {
            boolean knowsAlibi = faker.number().numberBetween(0, 100) < 50;

            if (!knowsAlibi) {
                stmt.append("Я не знаю, де він був у той час.");
                return stmt.toString();
            }

            if (isTargetKiller) {
                if(hasAliby) stmt.append("Він точно не причетний. Я знаю, що він " + killerAlibi + ".");
                else stmt.append("Я не знаю де він був у той час");
            }
            else if (isTargetDecoy) {
                stmt.append("Він точно не був удома. Здається, він був " + decoySecret + ".");
            }
            else if (isTargetBystander) {
                stmt.append("Він казав, що йшов додому і збирався зайти щось купити.");
            }
            else if (isTargetRegular) {
                stmt.append("Здається, він " + regularAlibi + ".");
            }
        }

        return stmt.toString();
    }

    private Communication createCommunication(String caseId, String sender, String receiver, LocalDateTime time, String type, int duration, String message) {
        Communication comm = new Communication();
        comm.setCaseId(caseId);
        comm.setSenderPhone(sender);
        comm.setReceiverPhone(receiver);
        comm.setTimestamp(time);
        comm.setType(type);
        comm.setDuration(duration);
        comm.setMessageText(message);
        return comm;
    }

    private String buildDescription(Citizen person){
        StringBuilder description = new StringBuilder();
        description.append(person.getGender() ? "Чоловік" : "Жінка");

        if (person.getPhysicalTraits() != null && !person.getPhysicalTraits().isEmpty()) {
            String trait = faker.options().nextElement(person.getPhysicalTraits());
            description.append(", помічено прикмету: ").append(trait);
        }

        if (person.getVehicle() != null && faker.bool().bool()) {
            description.append(". Автомобіль: ")
                    .append(person.getVehicle().getColor())
                    .append(" ")
                    .append(person.getVehicle().getPlate());
        }

        return description.toString();
    }

    private GpsLog createGpsLog(String caseId, String citizenId, double x, double y, LocalDateTime timestamp) {
        GpsLog log = new GpsLog();
        log.setCaseId(caseId);
        log.setCitizenId(citizenId);
        log.setX(x);
        log.setY(y);
        log.setTimestamp(timestamp);
        return log;
    }

    private WeaponLicense createLicense(String caseId, Citizen citizen, WeaponType weaponType) {
        WeaponLicense license = new WeaponLicense();
        license.setCaseId(caseId);
        license.setCitizenId(citizen.getId());
        license.setWeaponType(weaponType.name());
        license.setSerialNumber(faker.bothify("SN-####-????"));
        license.setIssueDate(LocalDateTime.now().minusDays(faker.number().numberBetween(30, 365)).toLocalDate());
        return license;
    }

    private void addRelationship(Citizen citizen, String targetId, RelationType type, int affinity) {
        if (citizen.getRelationships() == null) {
            citizen.setRelationships(new ArrayList<>());
        }
        Citizen.Relationship rel = new Citizen.Relationship();
        rel.setCitizenId(targetId);
        rel.setRelationType(type);
        rel.setAffinity(affinity);
        citizen.getRelationships().add(rel);
    }

    private boolean hasRelationWith(Citizen citizen, String targetId) {
        if (citizen.getRelationships() == null) return false;
        return citizen.getRelationships().stream().anyMatch(r -> r.getCitizenId().equals(targetId));
    }

    private Optional<Citizen> findAnyRelation(Citizen citizen, List<Citizen> people) {
        if (citizen.getRelationships() == null || citizen.getRelationships().isEmpty()) {
            return Optional.empty();
        }

        Citizen.Relationship randomRel = faker.options().nextElement(citizen.getRelationships());

        return people.stream().filter(p -> p.getId().equals(randomRel.getCitizenId())).findFirst();
    }

    private String getLastName(String fullName) {
        String[] parts = fullName.split(" ");
        return parts.length > 1 ? parts[parts.length - 1] : fullName;
    }

    private String getFirstName(String fullName) {
        String[] parts = fullName.split(" ");
        return parts[0];
    }

    public List<String> getRandomTraits(int count) {
        List<String> traits = new ArrayList<>(List.of("tattoo", "glasses", "scar"));
        Collections.shuffle(traits);
        return traits.subList(0, count);
    }

    public boolean areConnected(Citizen c1, Citizen c2){
        if(c1.getRelationships() == null || c2.getRelationships() == null) return false;
        return c1.getRelationships().stream().anyMatch(r -> r.getCitizenId().equals(c2.getId()));
    }
}