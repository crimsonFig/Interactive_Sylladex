/////Defined constants
#define TRUE 1
#define SUCCESS 5
#define FALSE 0
#define NAMESIZE 12
#define EMPTY "EMPTY"

/////structs and typedefs
typedef struct
{
    char item[NAMESIZE + 1];    //12 for an card name. item title
    char captchaCode[8];        //7 long captcha code. Code for w/e
    int  inUse;                 //used to track if the card is empty or filled
} Card;

//ADD A STRUCTURE TO ACT AS A GROUND FOR NON INVENTORY ITEMS. base off of arrayList?

/////Prototypes
Card newCard();
