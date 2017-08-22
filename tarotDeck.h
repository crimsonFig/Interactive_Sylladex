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
    Card newCard()                                      ;
    TDModus newTDModus()                                ;
//Save and Load
    void TDsave(TDModus tdModus)                        ;
    void TDload()                                       ;
//Inventory IO
    Card TDtakeOutByIndex(TDModus deck, int fileIndex)  ;
    Card TDdrawFromTop(TDModus deck)                    ;
    void TDcapture(TDModus deck, char item[])           ;
//Utility
    void TDdrawInventory(TDModus tdModus)               ;
    void TDshuffle(TDModus deck)                        ;
    int isEmpty(TDModus deck)                           ;
//Node/Stack Functions
    void freeDeck(TDModus tdModus)                      ;
    void addCard(TDModus deck, Node *pNew)              ;
