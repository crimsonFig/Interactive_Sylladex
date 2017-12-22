/////Defined constants

/////structs and typedefs

typedef struct Node
{
    Card card;
    struct Node *pNext;
} Node;

typedef struct
{
    //The deck structure is a linked list "stack"
    int deckSize;
    Node *deckTop;
} TDModusImp;             //the actual inventory structure pfModus
typedef TDModusImp *TDModus;

/////Prototypes
//initializations
    void TDentry()                                      ;
    TDModus newTDModus()                                ;
//Save and Load
    void TDsave(TDModus deck)                           ;
    void TDload(TDModus deck)                           ;
//Inventory IO
    Card TDtakeOutByIndex(TDModus deck, int fileIndex)  ;
    Card TDdrawFromTop(TDModus deck)                    ;
    void TDcapture(TDModus deck, char item[])           ;
//Utility
    void TDdrawInventory(TDModus deck)                  ;
    void TDshuffle(TDModus deck)                        ;
    int TDisEmpty(TDModus deck)                         ;
//Node/Stack Functions
    void TDfreeDeck(TDModus deck)                       ;
    void TDaddCard(TDModus deck, Node *pNew)            ;
