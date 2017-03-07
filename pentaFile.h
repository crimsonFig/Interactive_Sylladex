/////Defined constants

/////structs and typedefs

typedef struct
{
    //the next 5 array's can hold card of a certain kind. These are 5 arrays of the Card type, a card is a defined string.
    Card weapons[5]    ;
    Card survival[5]   ;
    Card misc[5]       ;
    Card info[5]       ;
    Card keyCritical[5];
} PFModusImp;             //the actual inventory structure pfModus

typedef PFModusImp *PFModus;

typedef struct
{
    int isEmpty;
    Card heldItem;
} Hand;

/////Prototypes
//initializations
    Card newCard()                                      ;
    PFModus newPFModus()                                ;
//Save and Load
    void PFsave(PFModus pfModus)                        ;
    PFModus PFload()                                    ;
//Inventory IO
    Card PFtakeOutByIndex(Card folder[], int fileIndex) ;
    Card PFtakeOutByName(Card folder[], char value[])   ;
    int PFpush(Card folder[], char item[])              ;

    int PFforceEject(Card card[])                       ;
    int PFforceEjectAll(PFModus modus)                  ;
//Utility
    int PFisFull(Card folder[])                         ;
    void PFdrawInventory(PFModus pfModus)               ;
