import java.util.*;
import java.util.stream.Collectors;

class Player {

    public static int baseX;
    public static int baseY;

    public static int TOP_HERO_POSITION_X;
    public static int TOP_HERO_POSITION_Y;
    public static int MIDDLE_HERO_POSITION_X;
    public static int MIDDLE_HERO_POSITION_Y;
    public static int BOTTOM_HERO_POSITION_X;
    public static int BOTTOM_HERO_POSITION_Y;
    public static boolean BLUE_SIDE;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        baseX = in.nextInt(); // The corner of the map representing your base
        baseY = in.nextInt();
        BLUE_SIDE = baseX == 0;
        int heroesPerPlayer = in.nextInt(); // Always 3

        if (BLUE_SIDE) {
            TOP_HERO_POSITION_X = 6592;
            TOP_HERO_POSITION_Y = 482;
            MIDDLE_HERO_POSITION_X = 5904;
            MIDDLE_HERO_POSITION_Y = 3763;
            BOTTOM_HERO_POSITION_X = 1803;
            BOTTOM_HERO_POSITION_Y = 6091;
        } else {
            TOP_HERO_POSITION_X = 16647;
            TOP_HERO_POSITION_Y = 3128;
            MIDDLE_HERO_POSITION_X = 13049;
            MIDDLE_HERO_POSITION_Y = 4874;
            BOTTOM_HERO_POSITION_X = 11884;
            BOTTOM_HERO_POSITION_Y = 7494;
        }
        int myHealth, myMana, enemyHealth, enemyMana;

        Map<Integer, Monster> monsters = new HashMap<>();
        Hero[] myHeroes = new Hero[3];
        Hero[] enemyHeroes = new Hero[3];

        myHealth = in.nextInt();
        myMana = in.nextInt();
        enemyHealth = in.nextInt();
        enemyMana = in.nextInt();
        int entityCount = in.nextInt();
        for (int i = 0; i < entityCount; i++) {
            initializeEntities(monsters, myHeroes, enemyHeroes, in);
        }
        System.out.println("MOVE " + myHeroes[0].defensivePositionX + " " + myHeroes[0].defensivePositionX);
        System.out.println("MOVE " + myHeroes[1].defensivePositionX + " " + myHeroes[1].defensivePositionX);
        System.out.println("MOVE " + myHeroes[2].defensivePositionX + " " + myHeroes[2].defensivePositionX);

        // game loop
        while (true) {
            monsters = new HashMap<>();
            myHealth = in.nextInt();
            myMana = in.nextInt();
            enemyHealth = in.nextInt();
            enemyMana = in.nextInt();
            entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                updateEntities(monsters, myHeroes, enemyHeroes, in);
            }

            List<Monster> monstersToTarget = monsters.values().stream()
                    .filter(m -> m.dangerousness != 0)
                    .sorted(Comparator.comparing(Monster::getDangerousness).reversed())
                    .collect(Collectors.toList());

            for (int i = 0; i < heroesPerPlayer; i++) {
                int hero = i;
                String action = monstersToTarget.stream()
                        .filter(m -> m.getClosestHero(myHeroes) == hero)
                        .map(m -> "MOVE " + m.x + " " + m.y + " KILL")
                        .findFirst()
                        .orElse("MOVE " + myHeroes[i].defensivePositionX + " " + myHeroes[i].defensivePositionY + " REPLACE");
                System.out.println(action);
            }
        }
    }

    private static void initializeEntities(Map<Integer, Monster> monsters, Hero[] myHeroes, Hero[] enemyHeroes, Scanner in) {
        int id = in.nextInt(); // Unique identifier
        int type = in.nextInt(); // 0=monster, 1=your hero, 2=opponent hero
        int x = in.nextInt(); // Position of this entity
        int y = in.nextInt();
        int shieldLife = in.nextInt(); // Ignore for this league; Count down until shield spell fades
        int isControlled = in.nextInt(); // Ignore for this league; Equals 1 when this entity is under a control spell
        int health = in.nextInt(); // Remaining health of this monster
        int vx = in.nextInt(); // Trajectory of this monster
        int vy = in.nextInt();
        int nearBase = in.nextInt(); // 0=monster with no target yet, 1=monster targeting a base
        int threatFor = in.nextInt(); // Given this monster's trajectory, is it a threat to 1=your base, 2=your opponent's base, 0=neither

        switch (type) {
            case 0:
                if (threatFor == 1)
                    monsters.put(id, new Monster(x, y, shieldLife, isControlled, health, vx, vy, nearBase, threatFor));
                break;
            case 1:
                myHeroes[id % 3] = new Hero(x, y, shieldLife, isControlled);
                myHeroes[id % 3].setDefensivePosition();
                break;
            case 2:
                enemyHeroes[id % 3] = new Hero(x, y, shieldLife, isControlled);
                break;
        }
    }

    private static void updateEntities(Map<Integer, Monster> monsters, Hero[] myHeroes, Hero[] enemyHeroes, Scanner in) {
        int id = in.nextInt(); // Unique identifier
        int type = in.nextInt(); // 0=monster, 1=your hero, 2=opponent hero
        int x = in.nextInt(); // Position of this entity
        int y = in.nextInt();
        int shieldLife = in.nextInt(); // Ignore for this league; Count down until shield spell fades
        int isControlled = in.nextInt(); // Ignore for this league; Equals 1 when this entity is under a control spell
        int health = in.nextInt(); // Remaining health of this monster
        int vx = in.nextInt(); // Trajectory of this monster
        int vy = in.nextInt();
        int nearBase = in.nextInt(); // 0=monster with no target yet, 1=monster targeting a base
        int threatFor = in.nextInt(); // Given this monster's trajectory, is it a threat to 1=your base, 2=your opponent's base, 0=neither

        switch (type) {
            case 0:
                if (threatFor == 1)
                    monsters.put(id, new Monster(x, y, shieldLife, isControlled, health, vx, vy, nearBase, threatFor));
                break;
            case 1:
                myHeroes[id % 3].update(x, y, shieldLife, isControlled);
                break;
            case 2:
                enemyHeroes[id % 3] = new Hero(x, y, shieldLife, isControlled);
                break;
        }
    }

    static class Hero {

        enum DefensivePosition {TOP, MIDDLE, BOTTOM}

        private int x;
        private int y;
        private int shieldLife;
        private int isControlled;
        private DefensivePosition defensivePosition;
        private int defensivePositionX;
        private int defensivePositionY;

        public Hero(int x, int y, int shieldLife, int isControlled) {
            this.update(x, y, shieldLife, isControlled);
        }

        public void setDefensivePosition() {
            if (BLUE_SIDE) {
                if (this.x == 849) {
                    this.defensivePosition = DefensivePosition.BOTTOM;
                    this.defensivePositionX = 1803;
                    this.defensivePositionY = 6091;
                } else if (this.x == 1131) {
                    this.defensivePosition = DefensivePosition.MIDDLE;
                    this.defensivePositionX = 5322;
                    this.defensivePositionY = 4345;
                } else {
                    this.defensivePosition = DefensivePosition.TOP;
                    this.defensivePositionX = 6645;
                    this.defensivePositionY = 1381;
                }

            } else {
                if (this.x == 16216) {
                    this.defensivePosition = DefensivePosition.BOTTOM;
                    this.defensivePositionX = 11884;
                    this.defensivePositionY = 7494;
                } else if (this.x == 16499) {
                    this.defensivePosition = DefensivePosition.MIDDLE;
                    this.defensivePositionX = 13049;
                    this.defensivePositionY = 4874;
                } else {
                    this.defensivePosition = DefensivePosition.TOP;
                    this.defensivePositionX = 16647;
                    this.defensivePositionY = 3128;
                }
            }
        }

        public void update(int x, int y, int shieldLife, int isControlled) {
            this.y = y;
            this.x = x;
            this.shieldLife = shieldLife;
            this.isControlled = isControlled;
        }
    }

    static class Monster {
        private int x;
        private int y;
        private int shieldLife;
        private int isControlled;
        private int health;
        private int vx;
        private int vy;
        private int nearBase;
        private int threatFor;

        private double dangerousness;

        public Monster(int x, int y, int shieldLife, int isControlled, int health, int vx, int vy, int nearBase, int threatFor) {
            this.y = y;
            this.x = x;
            this.shieldLife = shieldLife;
            this.isControlled = isControlled;
            this.health = health;
            this.vx = vx;
            this.vy = vy;
            this.nearBase = nearBase;
            this.threatFor = threatFor;
            this.dangerousness = 1.0 / distanceFromBase();
        }

        public int distanceFromBase() {
            return Math.abs(baseX - this.x) + Math.abs(baseY - this.y);
        }

        public int distanceFromHero(Hero hero) {
            return Math.abs(hero.x - this.x) + Math.abs(hero.y - this.y);
        }

        public double getDangerousness() {
            return dangerousness;
        }

        public int getClosestHero(Hero[] heroes) {
            double hero0 = distanceFromHero(heroes[0]);
            double hero1 = distanceFromHero(heroes[1]);
            double hero2 = distanceFromHero(heroes[2]);
            if (hero0 <= hero1 && hero0 <= hero2) return 0;
            if (hero1 <= hero2) return 1;
            return 2;
        }
    }
}