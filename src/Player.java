import java.util.*;

class Player {

    public static int baseX;
    public static int baseY;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        baseX = in.nextInt(); // The corner of the map representing your base
        baseY = in.nextInt();
        int heroesPerPlayer = in.nextInt(); // Always 3
        int myHealth, myMana, enemyHealth, enemyMana;

        SortedSet<Monster> monsters = new TreeSet<>(Comparator.comparing(Monster::getDangerousness).reversed());
        Hero[] myHeroes = new Hero[3];
        Hero[] enemyHeroes = new Hero[3];

        // game loop
        while (true) {
            myHealth = in.nextInt();
            myMana = in.nextInt();
            enemyHealth = in.nextInt();
            enemyMana = in.nextInt();
            int entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                updateEntities(monsters, myHeroes, enemyHeroes, in);
            }

            for (int i = 0; i < heroesPerPlayer; i++) {
                if (monsters.isEmpty() || monsters.first().dangerousness == 0.0) {
                    System.out.println("WAIT");
                } else {
                    int hero = i;
                    Monster monster = monsters.stream()
                            .filter(m -> m.getClosestHero(myHeroes) == hero)
                            .findFirst()
                            .orElse(monsters.first());
                    int x = monster.x;
                    int y = monster.y;
                    System.out.println("MOVE " + x + " " + y);
                    monsters.remove(monster);
                }
            }
        }
    }

    private static void updateEntities(SortedSet<Monster> monsters, Hero[] myHeroes, Hero[] enemyHeroes, Scanner in) {
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
                if (threatFor == 1) monsters.add(new Monster(id, x, y, shieldLife, isControlled, health, vx, vy, nearBase, threatFor));
                break;
            case 1:
                myHeroes[id % 3] = new Hero(x, y, shieldLife, isControlled);
                break;
            case 2:
                enemyHeroes[id % 3] = new Hero(x, y, shieldLife, isControlled);
                break;
        }
    }

    static class Hero {
        private int x;
        private int y;
        private int shieldLife;
        private int isControlled;

        public Hero(int x, int y, int shieldLife, int isControlled) {
            this.update(x, y, shieldLife, isControlled);
        }

        public void update(int x, int y, int shieldLife, int isControlled) {
            this.y = y;
            this.x = x;
            this.shieldLife = shieldLife;
            this.isControlled = isControlled;
        }
    }

    static class Monster {

        int id;
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

        public Monster(int id, int x, int y, int shieldLife, int isControlled, int health, int vx, int vy, int nearBase, int threatFor) {
            this.y = y;
            this.x = x;
            this.shieldLife = shieldLife;
            this.isControlled = isControlled;
            this.health = health;
            this.vx = vx;
            this.vy = vy;
            this.nearBase = nearBase;
            this.threatFor = threatFor;
            this.dangerousness = 1.0 / computeDistanceFromBase();
        }

        public int computeDistanceFromBase() {
            return (int) Math.sqrt(Math.pow(this.x - baseX, 2) + Math.pow(this.y - baseY, 2));
        }

        public int computeDistanceFromHero(Hero hero) {
            return (int) Math.sqrt(Math.pow(this.x - hero.x, 2) + Math.pow(this.y - hero.y, 2));
        }

        public double getDangerousness() {
            return dangerousness;
        }

        public int getClosestHero(Hero[] heroes) {
            double hero0 = computeDistanceFromHero(heroes[0]);
            double hero1 = computeDistanceFromHero(heroes[1]);
            double hero2 = computeDistanceFromHero(heroes[2]);
            if (hero0 <= hero1 && hero0 <= hero2) return 0;
            if (hero1 <= hero2) return 1;
            return 2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Monster monster = (Monster) o;
            return id == monster.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}