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
    Node *deckHead
} TDModusImp;             //the actual inventory structure pfModus
typedef TDModusImp *TDModus;

/////Prototypes
//initializations
    void TDentry()                                      ;
    Card newCard()                                      ;
    TDModus newTDModus()                                ;
//Save and Load
    void TDsave(TDModus tdModus)                        ;
    TDModus TDload()                                    ;
//Inventory IO
    Card TDtakeOutByIndex(TDModus deck, int fileIndex)  ;
    Card TDdrawFromTop(TDModus deck)                    ;
    int TDcapture(TDModus deck, char item[])            ;

    int TDforceEjectAll(TDModus tdModus)                ;
//Utility
    void TDdrawInventory(TDModus tdModus)               ;
    Card TDshuffle(TDModus deck)                        ;
